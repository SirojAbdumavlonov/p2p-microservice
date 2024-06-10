package com.example.user.dto;

import java.math.BigDecimal;

public record ServicePaymentRequest(Integer serviceId, Integer senderCardId,
                                    Integer accountId, BigDecimal amount)
        implements PaymentRequest{
}
