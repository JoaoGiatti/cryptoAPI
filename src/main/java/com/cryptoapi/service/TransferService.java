package com.cryptoapi.service;

import com.cryptoapi.dto.*;
import com.cryptoapi.entity.Transaction;
import com.cryptoapi.enums.TransactionStatus;
import com.cryptoapi.exception.InsufficientFundsException;
import com.cryptoapi.exception.InvalidAddressException;
import com.cryptoapi.exception.TransactionNotFoundException;
import com.cryptoapi.repository.TransactionRepository;
import com.cryptoapi.util.AddressValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final BinanceService binanceService;
    private final FeeCalculatorService feeCalculatorService;
    private final AddressValidator addressValidator;
    private final NotificationService notificationService;

    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        // Validate destination address
        if (!addressValidator.isValid(request.getToAddress(), request.getCurrency())) {
            throw new InvalidAddressException("Invalid " + request.getCurrency() + " address: " + request.getToAddress());
        }

        // Check balance
        BalanceResponse balance = binanceService.getWalletBalance(request.getCurrency());
        BigDecimal fee = feeCalculatorService.calculateFee(request.getCurrency(), request.getAmount());
        BigDecimal totalRequired = request.getAmount().add(fee);

        if (balance.getFree().compareTo(totalRequired) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Required: " + totalRequired + " " + request.getCurrency() +
                ", Available: " + balance.getFree() + " " + request.getCurrency()
            );
        }

        // Create transaction record
        String txId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Transaction transaction = Transaction.builder()
                .txId(txId)
                .fromAddress(request.getFromAddress())
                .toAddress(request.getToAddress())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .fee(fee)
                .status(TransactionStatus.PENDING)
                .network(request.getNetwork())
                .memo(request.getMemo())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction {} created. Initiating Binance withdrawal...", txId);

        // Execute async withdrawal
        executeWithdrawalAsync(transaction);

        return mapToResponse(transaction);
    }

    @Async
    protected void executeWithdrawalAsync(Transaction transaction) {
        try {
            BinanceWithdrawRequest withdrawRequest = BinanceWithdrawRequest.builder()
                    .coin(transaction.getCurrency())
                    .address(transaction.getToAddress())
                    .amount(transaction.getAmount())
                    .network(transaction.getNetwork())
                    .memo(transaction.getMemo())
                    .build();

            String binanceTxId = binanceService.executeWithdrawal(withdrawRequest);
            transaction.setBinanceTxId(binanceTxId);
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            notificationService.sendTransferNotification(transaction, "Transfer is being processed");
            log.info("Transaction {} submitted to Binance. Binance TxID: {}", transaction.getTxId(), binanceTxId);
        } catch (Exception e) {
            log.error("Failed to execute withdrawal for {}: {}", transaction.getTxId(), e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transactionRepository.save(transaction);
            notificationService.sendTransferNotification(transaction, "Transfer failed: " + e.getMessage());
        }
    }

    public TransferResponse getTransferStatus(String txId) {
        Transaction transaction = transactionRepository.findByTxId(txId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + txId));
        return mapToResponse(transaction);
    }

    public List<TransferResponse> getTransferHistory(int page, int size) {
        Page<Transaction> transactions = transactionRepository.findAll(PageRequest.of(page, size));
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransferResponse cancelTransfer(String txId) {
        Transaction transaction = transactionRepository.findByTxId(txId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + txId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be cancelled. Current status: " + transaction.getStatus());
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        log.info("Transaction {} cancelled.", txId);

        return mapToResponse(transaction);
    }

    private TransferResponse mapToResponse(Transaction transaction) {
        return TransferResponse.builder()
                .txId(transaction.getTxId())
                .binanceTxId(transaction.getBinanceTxId())
                .fromAddress(transaction.getFromAddress())
                .toAddress(transaction.getToAddress())
                .currency(transaction.getCurrency())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .status(transaction.getStatus().name())
                .network(transaction.getNetwork())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .errorMessage(transaction.getErrorMessage())
                .build();
    }
}
