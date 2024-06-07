package com.example.card.exception;

import lombok.NonNull;

public class CardNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public CardNotFoundException(@NonNull Integer cardId) {
        super(String.format("Card with id %s not found", cardId));
    }
}
