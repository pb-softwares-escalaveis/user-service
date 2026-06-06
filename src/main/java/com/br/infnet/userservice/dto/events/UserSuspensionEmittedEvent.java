package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserSuspensionEmittedEvent(
        UUID userId,
        String reason,
        int days,
        Instant occurredAt
){
}
