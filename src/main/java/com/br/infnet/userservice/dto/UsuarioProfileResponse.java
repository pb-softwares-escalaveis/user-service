package com.br.infnet.userservice.dto;

public record UsuarioProfileResponse(
        String username,
        String profilePicture,
        Float reputacao
) {
}
