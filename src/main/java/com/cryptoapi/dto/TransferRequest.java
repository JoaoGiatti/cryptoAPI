package com.cryptoapi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Source address is required")
    private String fromAddress;

    @NotBlank(message = "Destination address is required")
    private String toAddress;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "Invalid currency format")
    private String currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00000001", message = "Amount must be greater than 0")
    @Digits(integer = 20, fraction = 18)
    private BigDecimal amount;

    private String network;
    private String memo;
}
