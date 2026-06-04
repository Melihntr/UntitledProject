package com.project.transaction.domain.usecase;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private GetTransactionHistoryHandler handler;

    @Test
    void handle_withValidDateRange_delegatesToPort_andReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 0, 0);

        TransactionRecordModel r1 = TransactionRecordModel.builder()
                .id("t1").senderUserId("alice").receiverUserId("bob").amount(5.0)
                .transactionDate(LocalDateTime.of(2025,1,1,1,0)).status("COMPLETED").build();
        TransactionRecordModel r2 = TransactionRecordModel.builder()
                .id("t2").senderUserId("alice").receiverUserId("carol").amount(7.0)
                .transactionDate(LocalDateTime.of(2025,1,1,2,0)).status("COMPLETED").build();

        Page<TransactionRecordModel> expectedPage = new PageImpl<>(List.of(r1, r2), pageable, 2);

        when(transactionPort.getTransactionHistory("alice", start, end, pageable)).thenReturn(expectedPage);

        GetTransactionHistoryHandler.HistoryFilterInput input = GetTransactionHistoryHandler.HistoryFilterInput.builder()
                .userId("alice")
                .startDate(start)
                .endDate(end)
                .pageable(pageable)
                .build();

        // Act
        Page<TransactionRecordModel> result = handler.handle(input);

        // Assert
        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(transactionPort, times(1)).getTransactionHistory("alice", start, end, pageable);
        verifyNoMoreInteractions(transactionPort);
    }

    @Test
    void handle_whenStartDateIsAfterEndDate_throwsIllegalArgumentException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime start = LocalDateTime.of(2025, 1, 3, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 0, 0);

        GetTransactionHistoryHandler.HistoryFilterInput input = GetTransactionHistoryHandler.HistoryFilterInput.builder()
                .userId("alice")
                .startDate(start)
                .endDate(end)
                .pageable(pageable)
                .build();

        // Act / Assert
        assertThatThrownBy(() -> handler.handle(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date cannot be after the end date");

        verifyNoInteractions(transactionPort);
    }
}