package com.cryptoapi.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class FeeCalculatorService {

    // Base fees in their respective currencies
    private static final Map<String, BigDecimal> BASE_FEES = Map.of(
        "BTC",  new BigDecimal("0.0005"),
        "ETH",  new BigDecimal("0.005"),
        "BNB",  new BigDecimal("0.0005"),
        "USDT", new BigDecimal("1.00"),
        "SOL",  new BigDecimal("0.01"),
        "TRX",  new BigDecimal("1.00"),
        "XRP",  new BigDecimal("0.25")
    );

    private static final BigDecimal PERCENTAGE_FEE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal DEFAULT_FEE = new BigDecimal("0.001");

    public BigDecimal calculateFee(String currency, BigDecimal amount) {
        BigDecimal baseFee = BASE_FEES.getOrDefault(currency.toUpperCase(), DEFAULT_FEE);
        BigDecimal percentageFee = amount.multiply(PERCENTAGE_FEE);
        return baseFee.add(percentageFee).setScale(8, RoundingMode.CEILING);
    }
}
