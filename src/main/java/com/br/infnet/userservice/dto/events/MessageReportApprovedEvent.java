package com.br.infnet.userservice.dto.events;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.UUID;

public record MessageReportApprovedEvent(
        UUID correlationId,
        Long auctionId,
        UUID sellerId,
        Long messageId,
        @JsonAlias({"reason", "removalReason"})
        String reason,
        Instant occurredAt
) {
}
