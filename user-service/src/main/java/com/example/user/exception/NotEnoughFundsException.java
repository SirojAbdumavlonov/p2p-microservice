package com.example.user.exception;

import lombok.NonNull;

public class NotEnoughFundsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public NotEnoughFundsException(@NonNull String message) {
        super(message);
    }
}
