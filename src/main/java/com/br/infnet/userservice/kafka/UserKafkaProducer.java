package com.br.infnet.userservice.kafka;

import com.br.infnet.userservice.dto.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<String, String>> sendUserCreated(UserCreatedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("users.account.created", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento", e);
        }
    }

    public CompletableFuture<SendResult<String, String>> sendUserSuspended(UserSuspendedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("users.account.suspended", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento", e);
        }
    }

    public CompletableFuture<SendResult<String, String>> sendUserDeleted(UserDeletedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("users.account.deleted", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento", e);
        }
    }

    public CompletableFuture<SendResult<String, String>> sendUserBanned(UserBannedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("users.account.banned", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento", e);
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<SendResult<String, String>> sendUserUnsuspended(UserBackFromSuspensionEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("users.account.restored", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento de reativação", e);
        }
    }

    //** Métodos para testar o listener do Kafka **//
    public CompletableFuture<SendResult<String, String>> sendAuctionReportApproved(AuctionReportApprovedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("reports.auction.approved", event.sellerId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento de report de leilão", e);
        }
    }

    public CompletableFuture<SendResult<String, String>> sendMessageReportApproved(MessageReportApprovedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("reports.message.approved", event.sellerId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento de report de mensagem", e);
        }
    }

    public CompletableFuture<SendResult<String, String>> sendPaymentFailed(PaymentFailedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send("transactions.status.closed-payment-failed", event.userId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento de falha de pagamento", e);
        }
    }
}