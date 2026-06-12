package com.br.infnet.userservice.dto;

import com.br.infnet.userservice.enums.Status;

import java.util.UUID;

public record UsuarioStatusResponse(
        UUID userId,
        Status status,
        boolean isAllowed
) {
}
