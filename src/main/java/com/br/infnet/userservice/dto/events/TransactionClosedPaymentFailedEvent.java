package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record TransactionClosedPaymentFailedEvent(
        UUID correlationId,
        Long transactionId,
        UUID userId,
        Instant occurredAt
) {
}
