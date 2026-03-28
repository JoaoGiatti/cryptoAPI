package com.cryptoapi.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferResponse {
    private String txId;
    private String binanceTxId;
    private String fromAddress;
    private String toAddress;
    private String currency;
    private BigDecimal amount;
    private BigDecimal fee;
    private String status;
    private String network;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String errorMessage;
}
