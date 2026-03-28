package com.cryptoapi.service;

import com.cryptoapi.config.BinanceConfig;
import com.cryptoapi.dto.BalanceResponse;
import com.cryptoapi.dto.PriceResponse;
import com.cryptoapi.dto.BinanceWithdrawRequest;
import com.cryptoapi.exception.BinanceApiException;
import com.cryptoapi.util.HmacSignatureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceService {

    private static final String BINANCE_BASE_URL = "https://api.binance.com";
    private static final String TICKER_PRICE_PATH = "/api/v3/ticker/price";
    private static final String ACCOUNT_PATH = "/api/v3/account";
    private static final String WITHDRAW_PATH = "/sapi/v1/capital/withdraw/apply";
    private static final String DEPOSIT_HISTORY_PATH = "/sapi/v1/capital/deposit/hisrec";

    private final BinanceConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HmacSignatureUtil signatureUtil;

    public PriceResponse getCurrentPrice(String symbol) {
        String url = BINANCE_BASE_URL + TICKER_PRICE_PATH + "?symbol=" + symbol;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            return PriceResponse.builder()
                    .symbol(node.get("symbol").asText())
                    .price(new BigDecimal(node.get("price").asText()))
                    .timestamp(Instant.now().toEpochMilli())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", symbol, e.getMessage());
            throw new BinanceApiException("Failed to fetch price for " + symbol);
        }
    }

    public BalanceResponse getWalletBalance(String currency) {
        long timestamp = Instant.now().toEpochMilli();
        String queryString = "timestamp=" + timestamp;
        String signature = signatureUtil.generateSignature(queryString, config.getSecretKey());
        String url = BINANCE_BASE_URL + ACCOUNT_PATH + "?" + queryString + "&signature=" + signature;

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode balances = root.get("balances");

            for (JsonNode balance : balances) {
                if (balance.get("asset").asText().equals(currency)) {
                    return BalanceResponse.builder()
                            .currency(currency)
                            .free(new BigDecimal(balance.get("free").asText()))
                            .locked(new BigDecimal(balance.get("locked").asText()))
                            .build();
                }
            }
            return BalanceResponse.builder().currency(currency).free(BigDecimal.ZERO).locked(BigDecimal.ZERO).build();
        } catch (Exception e) {
            log.error("Error fetching balance for {}: {}", currency, e.getMessage());
            throw new BinanceApiException("Failed to fetch balance for " + currency);
        }
    }

    public String executeWithdrawal(BinanceWithdrawRequest withdrawRequest) {
        long timestamp = Instant.now().toEpochMilli();
        Map<String, String> params = new HashMap<>();
        params.put("coin", withdrawRequest.getCoin());
        params.put("address", withdrawRequest.getAddress());
        params.put("amount", withdrawRequest.getAmount().toPlainString());
        params.put("timestamp", String.valueOf(timestamp));

        if (withdrawRequest.getNetwork() != null) {
            params.put("network", withdrawRequest.getNetwork());
        }
        if (withdrawRequest.getMemo() != null) {
            params.put("addressTag", withdrawRequest.getMemo());
        }

        String queryString = buildQueryString(params);
        String signature = signatureUtil.generateSignature(queryString, config.getSecretKey());
        String url = BINANCE_BASE_URL + WITHDRAW_PATH;

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(queryString + "&signature=" + signature, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            return node.get("id").asText();
        } catch (Exception e) {
            log.error("Error executing withdrawal: {}", e.getMessage());
            throw new BinanceApiException("Withdrawal execution failed: " + e.getMessage());
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", config.getApiKey());
        return headers;
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    }
}
