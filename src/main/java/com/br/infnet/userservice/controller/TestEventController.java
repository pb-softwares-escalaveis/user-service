package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.dto.events.AuctionReportApprovedEvent;
import com.br.infnet.userservice.dto.events.MessageReportApprovedEvent;
import com.br.infnet.userservice.dto.events.PaymentFailedEvent;
import com.br.infnet.userservice.kafka.UserKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/test/events")
@RequiredArgsConstructor
@Slf4j
@Profile("test")
public class TestEventController {
    private final UserKafkaProducer kafkaProducer;

    @PostMapping("/auction-report")
    public ResponseEntity<String> sendAuctionReport(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "Test auction report") String reason,
            @RequestParam(defaultValue = "12345") Long auctionId) {

        AuctionReportApprovedEvent event = new AuctionReportApprovedEvent(
                UUID.randomUUID(),
                auctionId,
                userId,
                reason,
                Instant.now()
        );

        log.info("Enviando evento de report de leilão: userId={}", userId);
        kafkaProducer.sendAuctionReportApproved(event);

        return ResponseEntity.ok("Evento enviado com sucesso!");
    }

    @PostMapping("/message-report")
    public ResponseEntity<String> sendMessageReport(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "Test message report") String reason,
            @RequestParam(defaultValue = "12345") Long auctionId,
            @RequestParam(defaultValue = "67890") Long messageId) {

        MessageReportApprovedEvent event = new MessageReportApprovedEvent(
                UUID.randomUUID(),
                auctionId,
                userId,
                messageId,
                reason,
                Instant.now()
        );

        log.info("Enviando evento de report de mensagem: userId={}", userId);
        kafkaProducer.sendMessageReportApproved(event);

        return ResponseEntity.ok("Evento enviado com sucesso!");
    }

    @PostMapping("/payment-failed")
    public ResponseEntity<String> sendPaymentFailed(
            @RequestParam UUID highestBidderId,
            @RequestParam(defaultValue = "125489") Long transactionId) {

        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID(),
                transactionId,
                highestBidderId,
                true,
                Instant.now()
        );

        log.info("Enviando evento de falha de pagamento: userId={}", highestBidderId);
        kafkaProducer.sendPaymentFailed(event);

        return ResponseEntity.ok("Evento enviado com sucesso!");
    }
}