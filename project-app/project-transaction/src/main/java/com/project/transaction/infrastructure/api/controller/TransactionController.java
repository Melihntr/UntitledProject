package com.project.transaction.infrastructure.api.controller;

import com.project.common.model.GenericResponse;
import com.project.common.security.CurrentUserProvider;
import com.project.transaction.infrastructure.api.dto.TransferRequest;
import com.project.transaction.infrastructure.api.dto.TransferResponse;
import com.project.transaction.infrastructure.api.mapper.TransactionApiMapper;
import com.project.transaction.infrastructure.api.security.TransactionAccessValidator;
import com.project.transaction.domain.usecase.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.handler.CheckSuspiciousTransfersHandler;
import com.project.transaction.domain.handler.DeleteWalletHandler;
import com.project.transaction.domain.handler.ExecuteTransferHandler;
import com.project.transaction.domain.handler.GetTransactionHistoryHandler;
import com.project.transaction.domain.usecase.DeleteWalletInput;
import com.project.transaction.domain.usecase.HistoryFilterInput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for managing financial transactions.
 * Exposes endpoints for money transfers, history retrieval, and administrative fraud detection.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final ExecuteTransferHandler executeTransferHandler;
    private final GetTransactionHistoryHandler getTransactionHistoryHandler;
    private final CheckSuspiciousTransfersHandler suspiciousTransfersHandler;
    private final DeleteWalletHandler deleteWalletHandler;
    private final TransactionApiMapper transactionApiMapper;
    private final TransactionAccessValidator accessValidator;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Initiates a money transfer between two users.
     * Validates that the user making the request is the actual sender.
     *
     * @param loggedInUserId The ID of the authenticated user, extracted from the header.
     * @param request        The payload containing sender, receiver, and amount details.
     * @return A generic response containing the transaction record details.
     */
    @PostMapping("/transfer")
    public ResponseEntity<GenericResponse<TransferResponse>> executeTransfer(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @Valid @RequestBody TransferRequest request) {

        accessValidator.validateSender(loggedInUserId, request.getSenderUserId());

        TransactionInput input = transactionApiMapper.toInput(request);
        TransactionRecordModel result = executeTransferHandler.handle(input);

        TransferResponse response = transactionApiMapper.toResponse(result);
        return ResponseEntity.ok(GenericResponse.success(response));
    }

    /**
     * Retrieves the paginated transaction history for a specific user.
     * Prevents IDOR (Insecure Direct Object Reference) by ensuring the requester matches the target user.
     *
     * @param userId         The target user ID provided in the request parameters.
     * @param startDate      The start date for the history filter.
     * @param endDate        The end date for the history filter.
     * @param pageable       Pagination and sorting parameters injected by Spring.
     * @return A generic response containing the paginated transaction history.
     */
    @GetMapping("/history")
    public ResponseEntity<GenericResponse<Object>> getTransactionHistory(
            @RequestParam("userId") String userId,
            @RequestParam String startDate, 
            @RequestParam String endDate,
            Pageable pageable) { 
        
        accessValidator.validateHistoryOwner(currentUserProvider.getUserId(), userId);
        
        HistoryFilterInput input = HistoryFilterInput.builder()
                .userId(userId)
                .startDate(LocalDateTime.parse(startDate))
                .endDate(LocalDateTime.parse(endDate))
                .pageable(pageable)
                .build();
                
        Page<TransactionRecordModel> resultPage = getTransactionHistoryHandler.handle(input);
        Page<TransferResponse> responsePage = resultPage.map(transactionApiMapper::toResponse);
        
        return ResponseEntity.ok(GenericResponse.success(responsePage));
    }

    /**
     * Generates a report of suspicious transactions (Fraud Detection).
     * This is a sensitive administrative endpoint that requires elevated privileges.
     *
     * @param role        The role of the requester, expected to be 'ADMIN'.
     * @return A generic response containing a list of suspicious transaction records.
     */
    @GetMapping("/fraud-report")
    public ResponseEntity<GenericResponse<List<Object[]>>> getFraudReport(
            @RequestHeader(value = "X-Role", defaultValue = "USER") String role) {

        accessValidator.validateAdminRole(role);

        List<Object[]> suspiciousRecords = suspiciousTransfersHandler.handle();
        return ResponseEntity.ok(GenericResponse.successList(suspiciousRecords));
    }
    /**
     * Deletes a user's wallet and all associated transactions.
     * This is a sensitive operation that requires strict access control.
     *
     * @param loggedInUserId The ID of the authenticated user, extracted from the header.
     * @return A generic response indicating the success of the deletion operation.
     */
    @DeleteMapping("/wallets/{walletId}")
    public ResponseEntity<GenericResponse<Void>> deleteWallet(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @PathVariable String walletId) {

        deleteWalletHandler.handle(new DeleteWalletInput(walletId, loggedInUserId));
        return ResponseEntity.ok(GenericResponse.success(null, "Wallet deleted successfully."));
    }
}
