package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserBackFromSuspensionEvent(
        UUID correlationId,
        UUID userId,
        String name,
        String email,
        Instant occurredAt
) {
}
