package com.br.infnet.userservice.kafka;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.*;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.UsuarioNotFoundException;
import com.br.infnet.userservice.penalty.PenaltyFactory;
import com.br.infnet.userservice.penalty.PenaltyStrategy;
import com.br.infnet.userservice.repository.UsuarioRepository;
import com.br.infnet.userservice.utils.CorrelationIdUtil;
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
        try {
            log.info("Aplicando penalidade para usuário: {}", userId);

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

        } catch (Exception e) {
            log.error("Erro ao aplicar penalidade para usuário {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private void emitirEvento(Usuario usuario, PenaltyStrategy strategy, String reason, Instant ocorridoEm) {
        try {
            UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
            if (strategy.deveEmitirEventoDeSuspensao()) {
                UserSuspendedEvent event = new UserSuspendedEvent(
                        correlationId,
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm,
                        usuario.getReputacao().getSuspensoAte()
                );
                kafkaProducer.sendUserSuspended(event).get(10, TimeUnit.SECONDS);
                log.info("Evento de suspensão emitido para usuário {}", usuario.getId());
            } else {
                UserBannedEvent event = new UserBannedEvent(
                        correlationId,
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm
                );
                kafkaProducer.sendUserBanned(event).get(10, TimeUnit.SECONDS);
                log.info("Evento de banimento emitido para usuário {}", usuario.getId());
            }
        } catch (Exception e) {
            log.error("Falha ao enviar evento de penalidade para o Kafka para usuário {}, motivo: {}",
                    usuario.getId(), reason, e);
            throw new RuntimeException("Falha ao enviar evento para o Kafka", e);
        }
    }

    // ============================================================
    // KAFKA LISTENERS
    // ============================================================

    @Transactional
    @KafkaListener(topics = "reviews.report.auction-approved")
    public void consumeAuctionReportApproved(AuctionReportApprovedEvent event) {
        UUID correlationId = event.correlationId() != null ?
                event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        CorrelationIdUtil.setCorrelationId(correlationId.toString());

        try {
            log.info("Recebido report aprovado para leilão: auctionId={}, sellerId={}",
                    event.auctionId(), event.sellerId());

            aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt());

        } catch (Exception e) {
            log.error("Erro ao processar evento de report aprovado: auctionId={}", event.auctionId(), e);
        } finally {
            CorrelationIdUtil.clear();
        }
    }

    @Transactional
    @KafkaListener(topics = "reviews.report.qa-approved")
    public void consumeMessageReportApproved(MessageReportApprovedEvent event) {
        UUID correlationId = event.correlationId() != null ?
                event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        CorrelationIdUtil.setCorrelationId(correlationId.toString());

        try {
            log.info("Recebido report aprovado para mensagem: messageId={}, sellerId={}",
                    event.messageId(), event.sellerId());

            aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt());

        } catch (Exception e) {
            log.error("Erro ao processar evento de report de mensagem: messageId={}", event.messageId(), e);
        } finally {
            CorrelationIdUtil.clear();
        }
    }

    @Transactional
    @KafkaListener(topics = "transactions.status.closed")
    public void consumePaymentFailedClosed(PaymentFailedEvent event) {
        UUID correlationId = event.correlationId() != null ?
                event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        CorrelationIdUtil.setCorrelationId(correlationId.toString());

        try {
            log.info("Transação {} com falha de pagamento",
                    event.transactionId());

            if (event.penalty()) {
                log.info("Penalidade será aplicada para usuário: {}", event.highestBidderId());
                aplicarPenalidadePorMarks(
                        event.highestBidderId(),
                        "Transação expirada por falta de pagamento",
                        event.occurredAt()
                );
            } else {
                log.info("Nenhuma penalidade aplicada para transação: {}", event.transactionId());
            }
        } catch (Exception e) {
            log.error("Erro ao processar evento de falha de pagamento: transactionId={}", event.transactionId(), e);
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}