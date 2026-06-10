package com.project.transaction.domain.usecase;

import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.NotificationResult;
import com.project.transaction.domain.exception.NotificationDeliveryException;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.NotificationPort;
import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExecuteTransferHandler.
 * Mocks the outbound ports to verify the happy path, error handling, and business rule enforcement.
 */
@ExtendWith(MockitoExtension.class)
class ExecuteTransferHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private ExecuteTransferHandler handler;

    @Captor
    private ArgumentCaptor<TransactionRecordModel> recordCaptor;

    @Test
    void handle_success_updatesWallets_savesRecord_and_sendsNotification() {
        // Arrange
        WalletModel sender = WalletModel.builder()
                .id("w-s")
                .userId("sender-1")
                .balance(100.0)
                .version(1L)
                .build();

        WalletModel receiver = WalletModel.builder()
                .id("w-r")
                .userId("receiver-1")
                .balance(50.0)
                .version(1L)
                .build();

        when(transactionPort.getWalletByUserId("sender-1")).thenReturn(sender);
        when(transactionPort.getWalletByUserId("receiver-1")).thenReturn(receiver);
        when(transactionPort.updateWallet(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionPort.saveTransactionRecord(any())).thenAnswer(inv -> savedRecord("tx-1", inv.getArgument(0)));
        when(notificationPort.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0))
                .thenReturn(new NotificationResult("notification-1", "tx-1", "RECORDED", false));

        TransactionInput input = TransactionInput.builder()
                .senderUserId("sender-1")
                .receiverUserId("receiver-1")
                .amount(25.0)
                .build();

        // Act
        TransactionRecordModel result = handler.handle(input);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(25.0);
        assertThat(result.getSenderUserId()).isEqualTo("sender-1");
        assertThat(result.getReceiverUserId()).isEqualTo("receiver-1");
        assertThat(result.getTransactionDate()).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");

        // Sender balance deducted, receiver increased
        assertThat(sender.getBalance()).isEqualTo(75.0);
        assertThat(receiver.getBalance()).isEqualTo(75.0);

        // Verify persistence interactions
        verify(transactionPort, times(1)).getWalletByUserId("sender-1");
        verify(transactionPort, times(1)).getWalletByUserId("receiver-1");
        verify(transactionPort, times(2)).updateWallet(any());
        verify(transactionPort, times(1)).saveTransactionRecord(recordCaptor.capture());

        TransactionRecordModel passedToSave = recordCaptor.getValue();
        assertThat(passedToSave.getId()).isNull();
        assertThat(passedToSave.getAmount()).isEqualTo(25.0);
        assertThat(passedToSave.getStatus()).isEqualTo("COMPLETED");

        verify(notificationPort).sendTransferReceivedNotification("tx-1", "receiver-1", 25.0);
    }

    @Test
    void handle_whenNotificationServiceFails_shouldNotPropagateException_andStillReturnSavedRecord() {
        // Arrange
        WalletModel sender = WalletModel.builder()
                .id("w-s")
                .userId("sender-2")
                .balance(60.0)
                .version(1L)
                .build();

        WalletModel receiver = WalletModel.builder()
                .id("w-r")
                .userId("receiver-2")
                .balance(10.0)
                .version(1L)
                .build();

        when(transactionPort.getWalletByUserId("sender-2")).thenReturn(sender);
        when(transactionPort.getWalletByUserId("receiver-2")).thenReturn(receiver);
        when(transactionPort.updateWallet(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionPort.saveTransactionRecord(any())).thenAnswer(inv -> savedRecord("tx-2", inv.getArgument(0)));

        // Simulate notification failure
        doThrow(new RuntimeException("notification down"))
                .when(notificationPort).sendTransferReceivedNotification("tx-2", "receiver-2", 20.0);

        TransactionInput input = TransactionInput.builder()
                .senderUserId("sender-2")
                .receiverUserId("receiver-2")
                .amount(20.0)
                .build();

        // Act
        TransactionRecordModel result = handler.handle(input);

        // Assert: no exception propagated, record was saved and returned
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(20.0);
        verify(transactionPort).saveTransactionRecord(any());
        verify(notificationPort).sendTransferReceivedNotification("tx-2", "receiver-2", 20.0);
    }

    @Test
    void handle_whenNotificationServiceRejectsRequest_shouldNotPropagateTypedException() {
        WalletModel sender = WalletModel.builder().id("w-s").userId("sender-3").balance(60.0).version(1L).build();
        WalletModel receiver = WalletModel.builder().id("w-r").userId("receiver-3").balance(10.0).version(1L).build();
        when(transactionPort.getWalletByUserId("sender-3")).thenReturn(sender);
        when(transactionPort.getWalletByUserId("receiver-3")).thenReturn(receiver);
        when(transactionPort.updateWallet(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionPort.saveTransactionRecord(any())).thenAnswer(inv -> savedRecord("tx-3", inv.getArgument(0)));
        when(notificationPort.sendTransferReceivedNotification("tx-3", "receiver-3", 20.0))
                .thenThrow(new NotificationDeliveryException(
                        "Invalid request", 400, "NOTIFICATION_REQUEST_INVALID", "trace-3", null));

        TransactionRecordModel result = handler.handle(TransactionInput.builder()
                .senderUserId("sender-3").receiverUserId("receiver-3").amount(20.0).build());

        assertThat(result.getId()).isEqualTo("tx-3");
        verify(notificationPort).sendTransferReceivedNotification("tx-3", "receiver-3", 20.0);
    }

    @Test
    void handle_whenSenderHasInsufficientBalance_throwsIllegalArgumentException_andDoesNotPersistOrNotify() {
        // Arrange
        WalletModel sender = WalletModel.builder()
                .id("w-s")
                .userId("poor-sender")
                .balance(5.0)
                .version(1L)
                .build();

        when(transactionPort.getWalletByUserId("poor-sender")).thenReturn(sender);

        TransactionInput input = TransactionInput.builder()
                .senderUserId("poor-sender")
                .receiverUserId("some-receiver")
                .amount(10.0)
                .build();

        // Act / Assert
        assertThatThrownBy(() -> handler.handle(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");

        // Ensure no further interactions occurred after the failure
        verify(transactionPort, times(1)).getWalletByUserId("poor-sender");
        verify(transactionPort, never()).updateWallet(any());
        verify(transactionPort, never()).saveTransactionRecord(any());
        verifyNoInteractions(notificationPort);
    }

    private TransactionRecordModel savedRecord(String id, TransactionRecordModel record) {
        return TransactionRecordModel.builder()
                .id(id)
                .senderUserId(record.getSenderUserId())
                .receiverUserId(record.getReceiverUserId())
                .amount(record.getAmount())
                .transactionDate(record.getTransactionDate())
                .status(record.getStatus())
                .build();
    }
}
