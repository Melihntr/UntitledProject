package com.project.transaction.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.transaction.domain.exception.NotificationDeliveryException;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.NotificationResult;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.NotificationPort;
import com.project.transaction.domain.port.TransactionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Core business use case handler for executing a financial balance transfer between two wallets.
 * This class orchestrates the primary domain logic and delegates external notification delivery
 * through an outbound port.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExecuteTransferHandler implements UseCaseHandler<TransactionRecordModel, TransactionInput> {

    private final TransactionPort transactionPort;
    private final NotificationPort notificationPort;

    /**
     * Executes the money transfer process.
     * The @Transactional annotation guarantees atomicity. If any database operation fails
     * (e.g., wallet not found, insufficient balance, or OptimisticLockException due to concurrent updates),
     * the entire transaction is automatically rolled back, preventing data inconsistency.
     *
     * @param input The validated domain input containing sender, receiver, and amount details.
     * @return The finalized transaction record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordModel handle(TransactionInput input) {
        log.info("transfer.execute.request senderUserId={} receiverUserId={} amount={}",
                input.getSenderUserId(), input.getReceiverUserId(), input.getAmount());

        WalletModel senderWallet = transactionPort.getWalletByUserId(input.getSenderUserId());
        senderWallet.deductBalance(input.getAmount());
        transactionPort.updateWallet(senderWallet);

        WalletModel receiverWallet = transactionPort.getWalletByUserId(input.getReceiverUserId());
        receiverWallet.addBalance(input.getAmount());
        transactionPort.updateWallet(receiverWallet);

        TransactionRecordModel record = TransactionRecordModel.builder()
                .senderUserId(input.getSenderUserId())
                .receiverUserId(input.getReceiverUserId())
                .amount(input.getAmount())
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        TransactionRecordModel savedRecord = transactionPort.saveTransactionRecord(record);
        log.info("transfer.execute.success transactionId={} senderUserId={} receiverUserId={} amount={} status={}",
                savedRecord.getId(), savedRecord.getSenderUserId(), savedRecord.getReceiverUserId(),
                savedRecord.getAmount(), savedRecord.getStatus());
        sendTransferNotification(savedRecord);

        return savedRecord;
    }

    private void sendTransferNotification(TransactionRecordModel savedRecord) {
        try {
            NotificationResult result = notificationPort.sendTransferReceivedNotification(
                    savedRecord.getId(),
                    savedRecord.getReceiverUserId(),
                    savedRecord.getAmount()
            );
            log.info("notification.dispatch.success transactionId={} notificationId={} recipientId={} status={} duplicate={}",
                    savedRecord.getId(), result.notificationId(), savedRecord.getReceiverUserId(),
                    result.status(), result.duplicate());
        } catch (NotificationDeliveryException exception) {
            log.error("notification.dispatch.rejected transactionId={} recipientId={} httpStatus={} errorCode={} traceId={} reason={}",
                    savedRecord.getId(), savedRecord.getReceiverUserId(), exception.getHttpStatus(),
                    exception.getErrorCode(), exception.getTraceId(), exception.getMessage(), exception);
        } catch (RuntimeException e) {
            log.error("notification.dispatch.failure transactionId={} recipientId={} reason={}",
                    savedRecord.getId(), savedRecord.getReceiverUserId(), e.getMessage(), e);
        }
    }
}
