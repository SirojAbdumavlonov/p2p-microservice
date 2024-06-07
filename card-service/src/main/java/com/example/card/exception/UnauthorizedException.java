package com.example.card.exception;

import lombok.NonNull;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public UnauthorizedException(@NonNull String errorMessage) {
        super(errorMessage);
    }
}
