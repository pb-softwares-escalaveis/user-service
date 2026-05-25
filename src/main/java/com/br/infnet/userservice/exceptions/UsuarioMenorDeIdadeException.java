package com.br.infnet.userservice.exceptions;

public class UsuarioMenorDeIdadeException extends RuntimeException {
    public UsuarioMenorDeIdadeException(String message) {
        super(message);
    }
}
