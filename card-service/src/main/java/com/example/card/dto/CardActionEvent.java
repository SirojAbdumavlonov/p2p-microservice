package com.example.card.dto;

public record CardActionEvent(String ownerEmail, String cardNumber,
                              String actionType) {
}
