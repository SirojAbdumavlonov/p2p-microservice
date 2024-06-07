package com.example.card.exception;

import lombok.NonNull;

public class CardAlreadyUsedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CardAlreadyUsedException(@NonNull String cardNumber) {
        super("Card number " + cardNumber + " already registered!");
    }
}
