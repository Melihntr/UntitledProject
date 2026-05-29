package com.project.transaction.api.controller;

import com.project.common.model.GenericResponse;
import com.project.transaction.api.dto.TransferRequest;
import com.project.transaction.api.dto.TransferResponse;
import com.project.transaction.api.mapper.TransactionApiMapper;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.usecase.CheckSuspiciousTransfersHandler;
import com.project.transaction.domain.usecase.ExecuteTransferHandler;
import com.project.transaction.domain.usecase.GetTransactionHistoryHandler;
import jakarta.validation.Valid;
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
public class TransactionController {

    private final ExecuteTransferHandler executeTransferHandler;
    private final GetTransactionHistoryHandler getTransactionHistoryHandler;
    private final CheckSuspiciousTransfersHandler suspiciousTransfersHandler;
    private final TransactionApiMapper transactionApiMapper;

    // Dependency Injection via constructor
    public TransactionController(ExecuteTransferHandler executeTransferHandler,
                                 GetTransactionHistoryHandler getTransactionHistoryHandler,
                                 CheckSuspiciousTransfersHandler suspiciousTransfersHandler,
                                 TransactionApiMapper transactionApiMapper) {
        this.executeTransferHandler = executeTransferHandler;
        this.getTransactionHistoryHandler = getTransactionHistoryHandler;
        this.suspiciousTransfersHandler = suspiciousTransfersHandler;
        this.transactionApiMapper = transactionApiMapper;
    }

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

        // SECURITY CHECK: Ensure the authenticated user is not trying to spend someone else's money.
        if (!loggedInUserId.equals(request.getSenderUserId())) {
            throw new IllegalArgumentException("Security Violation: You can only transfer money from your own wallet.");
        }

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
        
        // SECURITY CHECK: IDOR Prevention
        // Ensure the authenticated user is not trying to access someone else's financial records.
        if (!loggedInUserId.equals(userId)) {
            throw new IllegalArgumentException("Security Violation: You are not authorized to view this transaction history.");
        }
        
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

        // SECURITY CHECK: Role-Based Access Control (RBAC)
        // Fraud detection logic must be restricted to authorized risk management actors.
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Access Denied: Only administrators can access system-wide fraud reports.");
        }

        List<Object[]> suspiciousRecords = suspiciousTransfersHandler.handle();
        return ResponseEntity.ok(GenericResponse.success(suspiciousRecords));
    }
}