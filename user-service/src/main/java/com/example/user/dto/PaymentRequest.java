package com.example.user.dto;

import java.math.BigDecimal;

public interface PaymentRequest {
    BigDecimal amount();
    Integer senderCardId();
}
