package com.br.infnet.userservice.dto;

import java.util.UUID;

public record UsuarioProfileResponse(
        UUID id,
        String username,
        String profilePicture,
        Float reputacao
) {
}
