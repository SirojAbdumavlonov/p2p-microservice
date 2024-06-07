package com.example.user.exception;

import lombok.NonNull;

public class UsernameAlreadyUsedException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public UsernameAlreadyUsedException(@NonNull String username) {
        super("Username " + username + " already used!");
    }
}
