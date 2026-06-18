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
import org.jboss.logging.MDC;
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

    private void aplicarPenalidadePorMarks(UUID userId, String reason, Instant ocorridoEm, UUID correlationId) {
        try {
            if (correlationId == null) {
                log.warn("Correlation ID não encontrada no contexto de penalidade, gerando nova ID para rastreamento");
                MDC.put("correlationId", CorrelationIdUtil.generateCorrelationId());
            } else {
                MDC.put("correlationId", correlationId);
            }


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

            emitirEvento(usuario, strategy, reason, ocorridoEm, correlationId);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void emitirEvento(Usuario usuario, PenaltyStrategy strategy, String reason, Instant ocorridoEm, UUID correlationId) {
        try {
            UUID finalCorrelationId = correlationId != null ? correlationId : CorrelationIdUtil.getCorrelationIdAsUUID();
            if (strategy.deveEmitirEventoDeSuspensao()) {
                UserSuspendedEvent event = new UserSuspendedEvent(
                        finalCorrelationId,
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm,
                        usuario.getReputacao().getSuspensoAte()
                );
                kafkaProducer.sendUserSuspended(event).get(10, TimeUnit.SECONDS);
                log.info("Evento de suspensão emitido para usuário {}, com correlationId {}", usuario.getId(), correlationId);
            } else {
                UserBannedEvent event = new UserBannedEvent(
                        finalCorrelationId,
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        reason,
                        ocorridoEm
                );
                kafkaProducer.sendUserBanned(event).get(10, TimeUnit.SECONDS);
                log.info("Evento de banimento emitido para usuário {}, com correlationId {}", usuario.getId(), correlationId);
            }
        } catch (Exception e) {
            log.error("Falha ao enviar evento de penalidade para o Kafka para usuário {}, reason: {}, correlationId: {}",
                    usuario.getId(), reason, correlationId, e);
            throw new RuntimeException("Falha ao enviar evento para o Kafka", e);
        }
    }

    // ============================================================
    // KAFKA LISTENERS
    // ============================================================

    @Transactional
    @KafkaListener(topics = "reviews.report.auction-approved")
    public void consumeAuctionReportApproved(AuctionReportApprovedEvent event) {
        UUID correlationId = event.correlationId() != null ? event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        MDC.put("correlationId", correlationId.toString());
        try {
            log.info("Recebido report aprovado para leilão: auctionId={}, correlationId={}",
                    event.auctionId(), correlationId);
            aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt(), correlationId);
        } finally {
            MDC.remove("correlationId");
        }
    }

    @Transactional
    @KafkaListener(topics = "reviews.report.qa-approved")
    public void consumeMessageReportApproved(MessageReportApprovedEvent event) {
        UUID correlationId = event.correlationId() != null ? event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        MDC.put("correlationId", correlationId.toString());
        try {
            log.info("Recebido report aprovado para mensagem: messageId={}, correlationId={}",
                    event.messageId(), correlationId);
            aplicarPenalidadePorMarks(event.sellerId(), event.reason(), event.occurredAt(), correlationId);
        } finally {
            MDC.remove("correlationId");
        }
    }

    @Transactional
    @KafkaListener(topics = "transactions.status.closed")
    public void consumePaymentFailedClosed(PaymentFailedEvent event) {
        UUID correlationId = event.correlationId() != null ? event.correlationId() : CorrelationIdUtil.getCorrelationIdAsUUID();
        MDC.put("correlationId", correlationId.toString());
        try {
            log.info("Transação {} com falha de pagamento, correlationId={}",
                    event.transactionId(), correlationId);
            if (event.penalty()) {
                log.info("Penalidade será aplicada para usuário: {}", event.highestBidderId());
                aplicarPenalidadePorMarks(event.highestBidderId(), "Transação expirada por falta de pagamento",
                        event.occurredAt(), correlationId);
            }
        } finally {
            MDC.remove("correlationId");
        }
    }
}