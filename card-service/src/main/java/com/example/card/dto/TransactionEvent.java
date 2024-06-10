package com.example.card.dto;

import java.math.BigDecimal;
import java.util.Date;

public record TransactionEvent(String transactionType, String senderEmail,
         Integer sendCardId, String receiverEmail,
         Integer receiverCardId, BigDecimal amount, Date eventDate) {
}
