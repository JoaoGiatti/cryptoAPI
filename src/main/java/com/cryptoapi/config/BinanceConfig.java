package com.cryptoapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "binance")
@Data
public class BinanceConfig {
    private String apiKey;
    private String secretKey;
    private String baseUrl = "https://api.binance.com";
    private int timeoutSeconds = 30;
    private int maxRetries = 3;
}
