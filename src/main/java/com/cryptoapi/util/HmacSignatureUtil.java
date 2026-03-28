package com.cryptoapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
@Slf4j
public class HmacSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public String generateSignature(String data, String secret) {
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmac.init(keySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Error generating HMAC signature: {}", e.getMessage());
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
