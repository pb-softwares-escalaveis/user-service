package com.br.infnet.userservice.dto.events;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportApprovedEvent(
        UUID correlationId,
        Long auctionId,
        UUID sellerId,
        @JsonAlias({"reason", "removalReason"})
        String reason,
        Instant occurredAt
) {
}
