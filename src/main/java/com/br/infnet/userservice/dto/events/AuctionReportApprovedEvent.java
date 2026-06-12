package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportApprovedEvent(
        UUID correlationId,
        Long auctionId,
        UUID sellerId,
        String reason,
        Instant occurredAt
) {
}
