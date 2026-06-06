package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserBannedEvent(
        UUID correlationId,
        UUID userId,
        String name,
        String email,
        String reason,
        Instant ocurredAt
) {}
