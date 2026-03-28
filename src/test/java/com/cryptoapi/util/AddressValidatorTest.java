package com.cryptoapi.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressValidatorTest {

    private final AddressValidator validator = new AddressValidator();

    @Test void validEthAddress() {
        assertTrue(validator.isValid("0x1234567890abcdef1234567890abcdef12345678", "ETH"));
    }

    @Test void invalidEthAddress_tooShort() {
        assertFalse(validator.isValid("0x1234", "ETH"));
    }

    @Test void validBtcAddress_legacy() {
        assertTrue(validator.isValid("1A1zP1eP5QGefi2DMPTfTL5SLmv7Divf6a", "BTC"));
    }

    @Test void validTrxAddress() {
        assertTrue(validator.isValid("TLsV52sRDL79HXGGm9yzwKibb6BeruhUzy", "TRX"));
    }

    @Test void nullAddress_returnsFalse() {
        assertFalse(validator.isValid(null, "ETH"));
    }

    @Test void blankAddress_returnsFalse() {
        assertFalse(validator.isValid("   ", "BNB"));
    }
}
