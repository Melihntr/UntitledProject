package com.project.transaction.domain.usecase;

import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckSuspiciousTransfersHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private CheckSuspiciousTransfersHandler handler;

    @Test
    void handle_delegatesToPort_andReturnsResults() {
        List<Object[]> expected = List.of(new Object[]{"alice", 2L}, new Object[]{"bob", 1L});
        when(transactionPort.getSuspiciousTransfers()).thenReturn(expected);

        List<Object[]> result = handler.handle();

        assertThat(result).isSameAs(expected);
        verify(transactionPort, times(1)).getSuspiciousTransfers();
        verifyNoMoreInteractions(transactionPort);
    }

    @Test
    void handle_returnsEmptyList_whenPortReturnsEmpty() {
        when(transactionPort.getSuspiciousTransfers()).thenReturn(List.of());

        List<Object[]> result = handler.handle();

        assertThat(result).isEmpty();
        verify(transactionPort, times(1)).getSuspiciousTransfers();
        verifyNoMoreInteractions(transactionPort);
    }
}