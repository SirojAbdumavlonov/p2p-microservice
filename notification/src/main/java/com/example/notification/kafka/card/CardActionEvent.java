package com.example.notification.kafka.card;

public record CardActionEvent(String ownerEmail, String cardNumber,
                              String actionType) {
}
