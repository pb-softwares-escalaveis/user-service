package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserSuspendedEvent(
        UUID correlationId,
        UUID userId,
        String name,
        String email,
        String reason,
        Instant occurredAt,
        Instant suspendedUntil
) {}
