package com.example.user.exception;

import lombok.NonNull;

public class PhoneNumberNotUsedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public PhoneNumberNotUsedException(@NonNull String phoneNumber) {
        super("Phone number " + phoneNumber + " is not used");
    }
}
