package com.project.transaction.domain.usecase;

import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExecuteTransferHandler.
 * Mocks the TransactionPort and RestTemplate to verify the happy path, error handling, and business rule enforcement.
 */
@ExtendWith(MockitoExtension.class)
class ExecuteTransferHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExecuteTransferHandler handler;

    @Captor
    private ArgumentCaptor<TransactionRecordModel> recordCaptor;

    @Captor
    private ArgumentCaptor<org.springframework.http.HttpEntity> httpEntityCaptor;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handle_success_updatesWallets_savesRecord_and_sendsNotification_withProvidedTraceId() throws Exception {
        // Arrange
        MDC.put("traceId", "trace-abc-123");

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
        when(transactionPort.saveTransactionRecord(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("notified"));

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
        assertThat(passedToSave.getAmount()).isEqualTo(25.0);
        assertThat(passedToSave.getStatus()).isEqualTo("COMPLETED");

        // Verify notification call and that trace id from MDC was forwarded
        verify(restTemplate, times(1)).postForEntity(anyString(), httpEntityCaptor.capture(), eq(String.class));
        org.springframework.http.HttpEntity<Map<String, String>> capturedEntity = httpEntityCaptor.getValue();
        assertThat(capturedEntity).isNotNull();
        assertThat(capturedEntity.getHeaders().getFirst("X-Trace-Id")).isEqualTo("trace-abc-123");
        assertThat(capturedEntity.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        Map<String, String> payload = capturedEntity.getBody();
        assertThat(payload).containsEntry("recipientId", "receiver-1");
        assertThat(payload.get("message")).contains("25.0");
    }

    @Test
    void handle_whenNotificationServiceFails_shouldNotPropagateException_andStillReturnSavedRecord() throws Exception {
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
        when(transactionPort.saveTransactionRecord(any())).thenAnswer(inv -> inv.getArgument(0));

        // Simulate notification failure
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("notification down"));

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
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
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
        verifyNoInteractions(restTemplate);
    }
}