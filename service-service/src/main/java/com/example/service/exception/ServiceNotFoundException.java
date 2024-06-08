package com.example.service.exception;

import lombok.NonNull;

public class ServiceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ServiceNotFoundException(@NonNull Integer serviceId) {
        super("Service with id " + serviceId + " not found");
    }
}
