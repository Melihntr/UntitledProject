package com.project.transaction.domain.port;

import com.project.transaction.domain.model.NotificationResult;

public interface NotificationPort {

    NotificationResult sendTransferReceivedNotification(String transactionId, String recipientId, Double amount);
}
