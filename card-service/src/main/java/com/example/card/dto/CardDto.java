package com.example.card.dto;

import java.math.BigDecimal;
import java.util.Date;

public record CardDto(String number, String cvv,
                      String type, BigDecimal balance,
                      Date expirationDate, boolean isExpired) {
}