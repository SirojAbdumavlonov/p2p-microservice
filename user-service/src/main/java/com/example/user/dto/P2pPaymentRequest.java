package com.example.user.dto;

import java.math.BigDecimal;

public record P2pPaymentRequest(BigDecimal amount, Integer senderCardId,
                                Integer receiverCardId, Integer receiverId) {
}
