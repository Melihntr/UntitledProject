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
     * @param loggedInUserId The ID of the authenticated user, extracted from the header.
     * @param userId         The target user ID provided in the request parameters.
     * @param startDate      The start date for the history filter.
     * @param endDate        The end date for the history filter.
     * @param pageable       Pagination and sorting parameters injected by Spring.
     * @return A generic response containing the paginated transaction history.
     */
    @GetMapping("/history")
    public ResponseEntity<GenericResponse<Object>> getTransactionHistory(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @RequestParam("userId") String userId,
            @RequestParam String startDate, 
            @RequestParam String endDate,
            Pageable pageable) { 
        
        accessValidator.validateHistoryOwner(loggedInUserId, userId);
        
        GetTransactionHistoryHandler.HistoryFilterInput input = GetTransactionHistoryHandler.HistoryFilterInput.builder()
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
     * @param adminUserId The ID of the requester.
     * @param role        The role of the requester, expected to be 'ADMIN'.
     * @return A generic response containing a list of suspicious transaction records.
     */
    @GetMapping("/fraud-report")
    public ResponseEntity<GenericResponse<List<Object[]>>> getFraudReport(
            @RequestHeader("X-User-Id") String adminUserId,
            @RequestHeader(value = "X-Role", defaultValue = "USER") String role) {

        accessValidator.validateAdminRole(role);

        List<Object[]> suspiciousRecords = suspiciousTransfersHandler.handle();
        return ResponseEntity.ok(GenericResponse.success(suspiciousRecords));
    }

    @DeleteMapping("/wallets/{userId}")
    public ResponseEntity<GenericResponse<Void>> deleteWallet(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @PathVariable String userId) {

        accessValidator.validateWalletOwner(loggedInUserId, userId);
        deleteWalletHandler.handle(userId);
        return ResponseEntity.ok(GenericResponse.success(null, "Wallet deleted successfully."));
    }
}
