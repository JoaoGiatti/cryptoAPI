package com.cryptoapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthRequest {
    @NotBlank private String apiKey;
    @NotBlank private String apiSecret;
}
