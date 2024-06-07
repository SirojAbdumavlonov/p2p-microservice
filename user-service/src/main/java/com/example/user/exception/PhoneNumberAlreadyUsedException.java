package com.example.user.exception;

import lombok.NonNull;

public class PhoneNumberAlreadyUsedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public PhoneNumberAlreadyUsedException(@NonNull String phoneNumber) {
        super("Phone number already used: " + phoneNumber);
    }
}
