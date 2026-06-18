package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID correlationId,
        Long transactionId,
        UUID highestBidderId,
        boolean penalty,
        Instant occurredAt
) {
}
