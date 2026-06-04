package com.br.infnet.userservice.dto;

public record VendedorResponseInfo (
    String nome,
    String sobrenome,
    String username,
    Float reputacao,
    String cidade,
    String pais
    ) {}
