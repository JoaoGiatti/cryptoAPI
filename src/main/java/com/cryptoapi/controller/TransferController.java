package com.cryptoapi.controller;

import com.cryptoapi.dto.TransferRequest;
import com.cryptoapi.dto.TransferResponse;
import com.cryptoapi.dto.BalanceResponse;
import com.cryptoapi.dto.PriceResponse;
import com.cryptoapi.service.TransferService;
import com.cryptoapi.service.BinanceService;
import com.cryptoapi.exception.InsufficientFundsException;
import com.cryptoapi.exception.InvalidAddressException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transfer", description = "Crypto transfer endpoints")
public class TransferController {

    private final TransferService transferService;
    private final BinanceService binanceService;

    @PostMapping("/send")
    @Operation(summary = "Send cryptocurrency to an address")
    public ResponseEntity<TransferResponse> sendCrypto(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer request received: {} {} to {}", request.getAmount(), request.getCurrency(), request.getToAddress());
        TransferResponse response = transferService.initiateTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/status/{txId}")
    @Operation(summary = "Get transfer status by transaction ID")
    public ResponseEntity<TransferResponse> getTransferStatus(@PathVariable String txId) {
        log.info("Checking status for transaction: {}", txId);
        TransferResponse response = transferService.getTransferStatus(txId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get transfer history for authenticated user")
    public ResponseEntity<List<TransferResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<TransferResponse> history = transferService.getTransferHistory(page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/balance/{currency}")
    @Operation(summary = "Get wallet balance for a specific currency")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String currency) {
        BalanceResponse balance = binanceService.getWalletBalance(currency.toUpperCase());
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get current market price for a trading pair")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable String symbol) {
        PriceResponse price = binanceService.getCurrentPrice(symbol.toUpperCase());
        return ResponseEntity.ok(price);
    }

    @PostMapping("/cancel/{txId}")
    @Operation(summary = "Cancel a pending transfer")
    public ResponseEntity<TransferResponse> cancelTransfer(@PathVariable String txId) {
        log.info("Cancel request for transaction: {}", txId);
        TransferResponse response = transferService.cancelTransfer(txId);
        return ResponseEntity.ok(response);
    }
}
