package com.cryptoapi.repository;

import com.cryptoapi.entity.Transaction;
import com.cryptoapi.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTxId(String txId);

    Optional<Transaction> findByBinanceTxId(String binanceTxId);

    List<Transaction> findByStatus(TransactionStatus status);

    Page<Transaction> findByFromAddress(String fromAddress, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.createdAt < :before")
    List<Transaction> findStuckTransactions(TransactionStatus status, LocalDateTime before);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt >= :since")
    Long countCompletedSince(LocalDateTime since);
}
