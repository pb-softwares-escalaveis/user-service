package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserBanEmittedEvent(
        UUID userId,
        String reason,
        Instant occurredAt
) {
}
