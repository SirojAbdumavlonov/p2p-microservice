package com.example.user.entity;

import com.example.user.constant.TransactionStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Builder
@Table(name = "p2p_transactions")
public class P2pTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private BigDecimal amount;

    @Column(nullable = false)
    private Integer senderCardId;

    @Column(nullable = false)
    private Integer recipientCardId;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    public P2pTransaction() {

    }
}
