package com.br.infnet.userservice.dto;

public record UsuarioResponse(
    String nome,
    String sobrenome,
    String cpf,
    String email,
    String telefone,
    boolean isAllowed
) {
}
