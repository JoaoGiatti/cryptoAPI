package com.cryptoapi.exception;

import lombok.*;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private Long timestamp;
    private Map<String, String> details;
}
