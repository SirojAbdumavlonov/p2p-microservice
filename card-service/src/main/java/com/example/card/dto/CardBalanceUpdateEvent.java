package com.example.card.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class CardBalanceUpdateEvent {
    private BigDecimal amount;
    private BigDecimal cardBalance;
    private String eventType;//deducted or added
    private String cardNumber;
    private String email;
    private Date eventDate;
    private String transactionType;
    private String serviceName;
}
