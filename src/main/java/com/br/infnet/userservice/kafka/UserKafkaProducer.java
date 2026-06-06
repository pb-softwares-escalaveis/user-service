package com.br.infnet.userservice.kafka;

import com.br.infnet.userservice.dto.events.UserBannedEvent;
import com.br.infnet.userservice.dto.events.UserCreatedEvent;
import com.br.infnet.userservice.dto.events.UserDeletedEvent;
import com.br.infnet.userservice.dto.events.UserSuspendedEvent;
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
}