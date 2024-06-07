package com.example.card.exception;

import lombok.NonNull;

public class OwnerOfCardNotExistException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    public OwnerOfCardNotExistException(@NonNull Integer userId, @NonNull String cardNumber) {}
}
