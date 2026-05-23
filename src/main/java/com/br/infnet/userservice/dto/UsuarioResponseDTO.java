package com.br.infnet.userservice.dto;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String sobrenome,
        String username,
        String email,
        String cpf,
        String telefone,
        int marks,
        float reputacao
) {
}
