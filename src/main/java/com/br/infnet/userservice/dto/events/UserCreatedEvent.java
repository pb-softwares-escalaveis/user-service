package com.br.infnet.userservice.dto.events;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID correlationId,
        UUID userId,
        String nome,
        String sobrenome,
        String username,
        String fotoPerfil,
        String email,
        String cpf,
        String telefone,
        float nota,
        String pais,
        String estado,
        String cidade,
        String cep,
        Instant occurredAt
) {
}
