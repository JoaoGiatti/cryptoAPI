package com.cryptoapi.scheduler;

import com.cryptoapi.entity.Transaction;
import com.cryptoapi.enums.TransactionStatus;
import com.cryptoapi.repository.TransactionRepository;
import com.cryptoapi.service.BinanceService;
import com.cryptoapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSyncScheduler {

    private final TransactionRepository transactionRepository;
    private final BinanceService binanceService;
    private final NotificationService notificationService;

    /**
     * Every 30 seconds: poll Binance for status updates on PROCESSING transactions.
     */
    @Scheduled(fixedDelay = 30_000)
    public void syncProcessingTransactions() {
        List<Transaction> processing = transactionRepository.findByStatus(TransactionStatus.PROCESSING);
        if (processing.isEmpty()) return;

        log.info("Syncing {} PROCESSING transactions...", processing.size());
        for (Transaction tx : processing) {
            try {
                // In a real impl, poll Binance withdraw history by binanceTxId
                // String status = binanceService.getWithdrawStatus(tx.getBinanceTxId());
                // For demo, we simply log
                log.debug("Checking Binance status for txId={}", tx.getTxId());
            } catch (Exception e) {
                log.error("Failed to sync txId={}: {}", tx.getTxId(), e.getMessage());
            }
        }
    }

    /**
     * Every 5 minutes: detect and fail stuck PENDING transactions older than 10 minutes.
     */
    @Scheduled(fixedDelay = 300_000)
    public void failStuckTransactions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Transaction> stuck = transactionRepository.findStuckTransactions(TransactionStatus.PENDING, cutoff);

        if (!stuck.isEmpty()) {
            log.warn("Found {} stuck PENDING transactions. Marking as FAILED.", stuck.size());
            for (Transaction tx : stuck) {
                tx.setStatus(TransactionStatus.FAILED);
                tx.setErrorMessage("Transaction timed out after 10 minutes in PENDING state.");
                tx.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(tx);
                notificationService.sendTransferNotification(tx, "Transaction timed out");
            }
        }
    }
}
