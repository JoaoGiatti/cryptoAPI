package com.cryptoapi.entity;

import com.cryptoapi.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_id", columnList = "txId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String txId;

    @Column(length = 128)
    private String binanceTxId;

    @Column(nullable = false, length = 128)
    private String fromAddress;

    @Column(nullable = false, length = 128)
    private String toAddress;

    @Column(nullable = false, length = 20)
    private String currency;

    @Column(nullable = false, precision = 30, scale = 18)
    private BigDecimal amount;

    @Column(nullable = false, precision = 30, scale = 18)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 20)
    private String network;

    @Column(length = 256)
    private String memo;

    @Column(length = 512)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
