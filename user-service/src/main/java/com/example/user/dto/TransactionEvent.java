package com.example.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Data
public class TransactionEvent {
    private String senderEmail;
    private Integer sendCardId;
    private String receiverEmail;
    private Integer receiverCardId;
    private BigDecimal amount;
    private Date eventDate;
    private String transactionType;

}
