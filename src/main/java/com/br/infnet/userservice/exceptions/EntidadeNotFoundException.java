package com.br.infnet.userservice.exceptions;

public class EntidadeNotFoundException extends RuntimeException {
    public EntidadeNotFoundException(String message) {
        super(message);
    }
}
