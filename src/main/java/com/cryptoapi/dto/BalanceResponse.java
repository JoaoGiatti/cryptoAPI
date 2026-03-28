package com.cryptoapi.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BalanceResponse {
    private String currency;
    private BigDecimal free;
    private BigDecimal locked;
}
