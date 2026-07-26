package com.br.infnet.userservice.dto;

import java.time.Instant;
import java.util.UUID;

public record UsuarioProfileResponse(
        UUID id,
        String username,
        String profilePicture,
        String status,
        Instant suspensoAte,
        Float reputacao
) {
}
