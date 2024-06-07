package com.example.user.exception;

import lombok.NonNull;

public class UserNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    public UserNotFoundException(@NonNull Integer id) {
        super(String.format("User not found with id %d", id));
    }
    public UserNotFoundException(@NonNull String username) {
        super(String.format("User not found with username %s", username));
    }
}
