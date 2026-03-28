package com.cryptoapi.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BinanceWithdrawRequest {
    private String coin;
    private String address;
    private BigDecimal amount;
    private String network;
    private String memo;
}
