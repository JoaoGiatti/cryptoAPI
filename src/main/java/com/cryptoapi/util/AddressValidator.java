package com.cryptoapi.util;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class AddressValidator {

    private static final Map<String, Pattern> PATTERNS = Map.of(
        "BTC",  Pattern.compile("^(1|3|bc1)[A-HJ-NP-Za-km-z1-9]{25,62}$"),
        "ETH",  Pattern.compile("^0x[0-9a-fA-F]{40}$"),
        "BNB",  Pattern.compile("^(bnb1|0x)[0-9a-zA-Z]{38,42}$"),
        "SOL",  Pattern.compile("^[1-9A-HJ-NP-Za-km-z]{32,44}$"),
        "USDT", Pattern.compile("^(0x[0-9a-fA-F]{40}|T[A-Za-z1-9]{33})$"),
        "TRX",  Pattern.compile("^T[A-Za-z1-9]{33}$"),
        "XRP",  Pattern.compile("^r[0-9a-zA-Z]{24,34}$")
    );

    public boolean isValid(String address, String currency) {
        if (address == null || address.isBlank()) return false;
        Pattern pattern = PATTERNS.get(currency.toUpperCase());
        if (pattern == null) return address.length() >= 20; // fallback for unlisted currencies
        return pattern.matcher(address).matches();
    }
}
