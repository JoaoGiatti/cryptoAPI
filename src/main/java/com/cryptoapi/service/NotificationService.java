package com.cryptoapi.service;

import com.cryptoapi.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendTransferNotification(Transaction transaction, String message) {
        // Integration point for: email, SMS, webhooks, Firebase, etc.
        log.info("[NOTIFICATION] TxID={} | Status={} | Message={}",
                transaction.getTxId(), transaction.getStatus(), message);
        // TODO: integrate with email provider (SendGrid, SES, etc.)
        // TODO: integrate with webhook callback URL from the original request
    }
}
