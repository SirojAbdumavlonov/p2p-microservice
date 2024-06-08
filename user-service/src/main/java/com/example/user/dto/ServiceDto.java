package com.example.user.dto;

import java.math.BigDecimal;

public record ServiceDto(Integer id, String name, String description,
                         String status, BigDecimal price) {
}
