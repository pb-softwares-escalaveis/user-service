package com.br.infnet.userservice.dto;

public record VendedorResponseInfo (
    String nome,
    String sobrenome,
    String username,
    String fotoPerfil,
    Float nota,
    String cidade,
    String estado,
    String pais
    ) {}
