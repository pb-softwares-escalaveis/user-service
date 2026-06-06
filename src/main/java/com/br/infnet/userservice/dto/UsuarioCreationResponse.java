package com.br.infnet.userservice.dto;

import java.util.UUID;

public record UsuarioCreationResponse(UUID userId, String message) {
    public UsuarioCreationResponse(UUID userId) {
        this(userId, "Usuário criado com sucesso.");
    }
}
