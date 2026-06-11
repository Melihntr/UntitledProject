package com.project.transaction.api.controller;

import com.project.common.model.GenericResponse;
import com.project.transaction.api.dto.TransferRequest;
import com.project.transaction.api.dto.TransferResponse;
import com.project.transaction.api.mapper.TransactionApiMapper;
import com.project.transaction.api.security.TransactionAccessValidator;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.usecase.CheckSuspiciousTransfersHandler;
import com.project.transaction.domain.usecase.DeleteWalletHandler;
import com.project.transaction.domain.usecase.ExecuteTransferHandler;
import com.project.transaction.domain.usecase.GetTransactionHistoryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private ExecuteTransferHandler executeTransferHandler;

    @Mock
    private GetTransactionHistoryHandler getTransactionHistoryHandler;

    @Mock
    private CheckSuspiciousTransfersHandler suspiciousTransfersHandler;

    @Mock
    private DeleteWalletHandler deleteWalletHandler;

    @Mock
    private TransactionApiMapper transactionApiMapper;

    private TransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionController(
                executeTransferHandler,
                getTransactionHistoryHandler,
                suspiciousTransfersHandler,
                deleteWalletHandler,
                transactionApiMapper,
                new TransactionAccessValidator()
        );
    }

    @Test
    void executeTransfer_whenLoggedInUserDiffersFromSender_throwsIllegalArgumentException() {
        TransferRequest req = new TransferRequest();
        req.setSenderUserId("alice");
        req.setReceiverUserId("bob");
        req.setAmount(10.0);

        // logged in user is different
        assertThatThrownBy(() -> controller.executeTransfer("mallory", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only transfer money from your own wallet");

        verifyNoInteractions(transactionApiMapper, executeTransferHandler);
    }

    @Test
    void executeTransfer_whenAuthorized_callsHandlerAndReturnsMappedResponse() {
        TransferRequest req = new TransferRequest();
        req.setSenderUserId("alice");
        req.setReceiverUserId("bob");
        req.setAmount(25.5);

        TransactionInput input = TransactionInput.builder()
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(25.5)
                .build();

        TransactionRecordModel record = TransactionRecordModel.builder()
                .id("tx-1")
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(25.5)
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        TransferResponse responseDto = TransferResponse.builder()
                .transactionId("tx-1")
                .status("COMPLETED")
                .amount(25.5)
                .transactionDate(record.getTransactionDate())
                .build();

        when(transactionApiMapper.toInput(req)).thenReturn(input);
        when(executeTransferHandler.handle(input)).thenReturn(record);
        when(transactionApiMapper.toResponse(record)).thenReturn(responseDto);

        ResponseEntity<GenericResponse<TransferResponse>> resp = controller.executeTransfer("alice", req);

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        GenericResponse<TransferResponse> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isSameAs(responseDto);

        verify(transactionApiMapper).toInput(req);
        verify(executeTransferHandler).handle(input);
        verify(transactionApiMapper).toResponse(record);
    }

    @Test
    void getTransactionHistory_whenLoggedInUserDiffersFromUserId_throwsIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 10);
        String start = "2025-01-01T00:00:00";
        String end = "2025-01-02T00:00:00";

        assertThatThrownBy(() -> controller.getTransactionHistory("alice", "bob", start, end, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You are not authorized to view this transaction history");

        verifyNoInteractions(getTransactionHistoryHandler, transactionApiMapper);
    }

    @Test
    void getTransactionHistory_whenAuthorized_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        String start = "2025-01-01T00:00:00";
        String end = "2025-01-02T00:00:00";

        TransactionRecordModel r1 = TransactionRecordModel.builder()
                .id("t1").senderUserId("alice").receiverUserId("bob").amount(5.0)
                .transactionDate(LocalDateTime.now()).status("COMPLETED").build();
        TransactionRecordModel r2 = TransactionRecordModel.builder()
                .id("t2").senderUserId("alice").receiverUserId("carol").amount(7.0)
                .transactionDate(LocalDateTime.now()).status("COMPLETED").build();

        Page<TransactionRecordModel> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

        TransferResponse tr1 = TransferResponse.builder()
                .transactionId("t1").status("COMPLETED").amount(5.0).transactionDate(r1.getTransactionDate()).build();
        TransferResponse tr2 = TransferResponse.builder()
                .transactionId("t2").status("COMPLETED").amount(7.0).transactionDate(r2.getTransactionDate()).build();

        GetTransactionHistoryHandler.HistoryFilterInput expectedInput = GetTransactionHistoryHandler.HistoryFilterInput.builder()
                .userId("alice")
                .startDate(LocalDateTime.parse(start))
                .endDate(LocalDateTime.parse(end))
                .pageable(pageable)
                .build();

        when(getTransactionHistoryHandler.handle(any())).thenReturn(page);
        when(transactionApiMapper.toResponse(r1)).thenReturn(tr1);
        when(transactionApiMapper.toResponse(r2)).thenReturn(tr2);

        ResponseEntity<GenericResponse<Object>> resp = controller.getTransactionHistory("alice", "alice", start, end, pageable);

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        GenericResponse<Object> body = resp.getBody();
        assertThat(body).isNotNull();

        // The controller wraps a Page<TransferResponse> as data; assert contents
        Object data = body.getData();
        assertThat(data).isInstanceOf(Page.class);
        @SuppressWarnings("unchecked")
        Page<TransferResponse> respPage = (Page<TransferResponse>) data;
        assertThat(respPage.getTotalElements()).isEqualTo(2);
        assertThat(respPage.getContent()).containsExactly(tr1, tr2);

        verify(getTransactionHistoryHandler).handle(any());
        verify(transactionApiMapper).toResponse(r1);
        verify(transactionApiMapper).toResponse(r2);
    }

    @Test
    void getFraudReport_whenNonAdmin_throwsSecurityException() {
        assertThatThrownBy(() -> controller.getFraudReport("admin", "USER"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only administrators can access");

        verifyNoInteractions(suspiciousTransfersHandler);
    }

    @Test
    void getFraudReport_whenAdmin_returnsSuspiciousRecords() {
        List<Object[]> suspicious = List.of(new Object[]{"a", 1}, new Object[]{"b", 2});
        when(suspiciousTransfersHandler.handle()).thenReturn(suspicious);

        ResponseEntity<GenericResponse<List<Object[]>>> resp = controller.getFraudReport("admin", "ADMIN");

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        GenericResponse<List<Object[]>> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isSameAs(suspicious);

        verify(suspiciousTransfersHandler).handle();
    }

    @Test
    void deleteWallet_whenOwner_delegatesAndReturnsGenericSuccessResponse() {
        ResponseEntity<GenericResponse<Void>> response = controller.deleteWallet("alice", "wallet-1");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Wallet deleted successfully.");
        assertThat(response.getBody().getData()).isNull();
        verify(deleteWalletHandler).handle(new DeleteWalletHandler.DeleteWalletInput("wallet-1", "alice"));
    }
}
