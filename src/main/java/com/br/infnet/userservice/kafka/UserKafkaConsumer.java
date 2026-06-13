package com.br.infnet.userservice.kafka;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.*;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.UsuarioNotFoundException;
import com.br.infnet.userservice.penalty.PenaltyFactory;
import com.br.infnet.userservice.penalty.PenaltyStrategy;
import com.br.infnet.userservice.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserKafkaConsumer {
    private final UsuarioRepository usuarioRepository;
    private final UserKafkaProducer kafkaProducer;
    private final PenaltyFactory penalidadeFactory;

    private void aplicarPenalidadePorMarks(UUID userId, String reason, Instant ocorridoEm) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado: " + userId));

        if (usuario.getStatus() == Status.BANIDO) {
            log.info("Usuário {} já está banido, ignorando nova penalidade", usuario.getId());
            return;
        }

        Reputacao reputacao = usuario.getReputacao();
        if (reputacao == null) {
            reputacao = new Reputacao();
            usuario.setReputacao(reputacao);
        }

        int marksAtuais = reputacao.getMarks() != null ? reputacao.getMarks() : 3;
        PenaltyStrategy strategy = penalidadeFactory.getStrategy(marksAtuais);

        if (reputacao.getSuspensoAte() != null && reputacao.getSuspensoAte().isAfter(Instant.now())) {
            log.warn("Usuário {} está suspenso até {}, mas nova denúncia será acumulada",
                    usuario.getId(), reputacao.getSuspensoAte());
        }

        strategy.aplicar(usuario, reputacao, reason, ocorridoEm);
        usuarioRepository.save(usuario);

        log.info(strategy.getLogMessage(usuario));

        emitirEvento(usuario, strategy, reason, ocorridoEm);
    }

    private void emitirEvento(Usuario usuario, PenaltyStrategy strategy, String reason, Instant ocorridoEm) {
        try {
            if (strategy.deveEmitirEventoDeSuspensao()) {
                UserSuspendedEvent event = new UserSuspendedEvent(
                        UUID.randomUUID(),
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm,
                        usuario.getReputacao().getSuspensoAte()
                );
                kafkaProducer.sendUserSuspended(event).get(10, TimeUnit.SECONDS);
            } else {
                UserBannedEvent event = new UserBannedEvent(
                        UUID.randomUUID(),
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm
                );
                kafkaProducer.sendUserBanned(event).get(10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar evento para o Kafka", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "reports.auction.approved")
    public void consumeAuctionReportApproved(AuctionReportApprovedEvent event) {
        log.info("Recebido report aprovado para leilão de número auctionId={}", event.auctionId());
        aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt());
    }

    @Transactional
    @KafkaListener(topics = "reports.message.approved")
    public void consumeMessageReportApproved(MessageReportApprovedEvent event) {
        log.info("Recebido report aprovado para mensagem de número messageId={}", event.messageId());
        aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt());
    }

    @Transactional
    @KafkaListener(topics = "transactions.status.closed-payment-failed")
    public void consumePaymentFailedClosed(TransactionClosedPaymentFailedEvent event) {
        log.info("Recebida transação fechada com pagamento expirado transactionId={}", event.transactionId());
        aplicarPenalidadePorMarks(event.userId(), "Transação expirada por falta de pagamento", event.occurredAt());
    }
}