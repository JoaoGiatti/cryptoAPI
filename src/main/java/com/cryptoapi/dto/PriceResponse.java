package com.cryptoapi.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PriceResponse {
    private String symbol;
    private BigDecimal price;
    private Long timestamp;
}
