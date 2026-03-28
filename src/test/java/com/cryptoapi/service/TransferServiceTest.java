package com.cryptoapi.service;

import com.cryptoapi.dto.BalanceResponse;
import com.cryptoapi.dto.TransferRequest;
import com.cryptoapi.dto.TransferResponse;
import com.cryptoapi.entity.Transaction;
import com.cryptoapi.enums.TransactionStatus;
import com.cryptoapi.exception.InsufficientFundsException;
import com.cryptoapi.exception.InvalidAddressException;
import com.cryptoapi.repository.TransactionRepository;
import com.cryptoapi.util.AddressValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BinanceService binanceService;
    @Mock private FeeCalculatorService feeCalculatorService;
    @Mock private AddressValidator addressValidator;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TransferService transferService;

    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = TransferRequest.builder()
                .fromAddress("0xSenderAddress")
                .toAddress("0x1234567890abcdef1234567890abcdef12345678")
                .currency("ETH")
                .amount(new BigDecimal("0.5"))
                .network("ETH")
                .build();
    }

    @Test
    void initiateTransfer_success() {
        when(addressValidator.isValid(anyString(), anyString())).thenReturn(true);
        when(binanceService.getWalletBalance("ETH")).thenReturn(
                BalanceResponse.builder().currency("ETH").free(new BigDecimal("10.0")).locked(BigDecimal.ZERO).build()
        );
        when(feeCalculatorService.calculateFee("ETH", new BigDecimal("0.5"))).thenReturn(new BigDecimal("0.005"));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.initiateTransfer(validRequest);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("ETH", response.getCurrency());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void initiateTransfer_invalidAddress_throws() {
        when(addressValidator.isValid(anyString(), anyString())).thenReturn(false);
        assertThrows(InvalidAddressException.class, () -> transferService.initiateTransfer(validRequest));
    }

    @Test
    void initiateTransfer_insufficientFunds_throws() {
        when(addressValidator.isValid(anyString(), anyString())).thenReturn(true);
        when(binanceService.getWalletBalance("ETH")).thenReturn(
                BalanceResponse.builder().currency("ETH").free(new BigDecimal("0.001")).locked(BigDecimal.ZERO).build()
        );
        when(feeCalculatorService.calculateFee(anyString(), any())).thenReturn(new BigDecimal("0.005"));
        assertThrows(InsufficientFundsException.class, () -> transferService.initiateTransfer(validRequest));
    }

    @Test
    void cancelTransfer_pendingTransaction_success() {
        Transaction tx = Transaction.builder()
                .txId("TESTTX123")
                .status(TransactionStatus.PENDING)
                .currency("ETH")
                .amount(new BigDecimal("0.5"))
                .fee(new BigDecimal("0.005"))
                .build();

        when(transactionRepository.findByTxId("TESTTX123")).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.cancelTransfer("TESTTX123");
        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void cancelTransfer_processingTransaction_throws() {
        Transaction tx = Transaction.builder()
                .txId("TESTTX456")
                .status(TransactionStatus.PROCESSING)
                .build();

        when(transactionRepository.findByTxId("TESTTX456")).thenReturn(Optional.of(tx));
        assertThrows(IllegalStateException.class, () -> transferService.cancelTransfer("TESTTX456"));
    }
}
