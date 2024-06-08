package com.example.card.entity;

import com.example.card.constant.CardStatus;
import com.example.card.constant.CardType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "cards")
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 16)
    @Size(min = 16, max = 16, message = "card number consists of 16 digits!")
    private String number;

    @Column(nullable = false)
    @Size(min = 3, max = 3, message = "cvv should have 3 numbers!")
    private String cvv;

    @Column(nullable = false)
    private CardType type;

    @Column(nullable = false)
    private BigDecimal balance;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Column(nullable = false)
    private Integer userId;

    private boolean isExpired;

    public Card() {

    }
}
