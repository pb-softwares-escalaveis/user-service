package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReportApprovedEvent(
        String correlationId,
        Long auctionId,
        UUID sellerId,
        Long messageId,
        String reason,
        Instant occurredAt
) {
}
