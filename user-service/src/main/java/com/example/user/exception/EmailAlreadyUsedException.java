package com.example.user.exception;

import lombok.NonNull;

public class EmailAlreadyUsedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public EmailAlreadyUsedException(@NonNull String email) {
        super("email already used: " + email);
    }
}
