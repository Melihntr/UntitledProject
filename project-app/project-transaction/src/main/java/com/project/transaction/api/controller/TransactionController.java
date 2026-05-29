package com.project.transaction.api.controller;

import com.project.common.model.GenericResponse;
import com.project.transaction.api.dto.TransferRequest;
import com.project.transaction.api.dto.TransferResponse;
import com.project.transaction.api.mapper.TransactionApiMapper;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.usecase.CheckSuspiciousTransfersHandler;
import com.project.transaction.domain.usecase.ExecuteTransferHandler;
import com.project.transaction.domain.usecase.GetTransactionHistoryHandler; // Import eklendi
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final ExecuteTransferHandler executeTransferHandler;
    private final GetTransactionHistoryHandler getTransactionHistoryHandler; // 1. Değişken buraya eklendi
    private final TransactionApiMapper transactionApiMapper;

    // 2. Constructor içine eklenerek Spring'in enjekte etmesi sağlandı
    private final CheckSuspiciousTransfersHandler suspiciousTransfersHandler;

    public TransactionController(ExecuteTransferHandler executeTransferHandler,
                                 GetTransactionHistoryHandler getTransactionHistoryHandler,
                                 CheckSuspiciousTransfersHandler suspiciousTransfersHandler, // Eklendi
                                 TransactionApiMapper transactionApiMapper) {
        this.executeTransferHandler = executeTransferHandler;
        this.getTransactionHistoryHandler = getTransactionHistoryHandler;
        this.suspiciousTransfersHandler = suspiciousTransfersHandler; // Eklendi
        this.transactionApiMapper = transactionApiMapper;
    }

    @PostMapping("/transfer")
    public ResponseEntity<GenericResponse<TransferResponse>> executeTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        TransactionInput input = transactionApiMapper.toInput(request);
        TransactionRecordModel record = executeTransferHandler.handle(input);
        TransferResponse responseDto = transactionApiMapper.toResponse(record);
        
        return ResponseEntity.ok(GenericResponse.success(responseDto));
    }

    @GetMapping("/history")
    public ResponseEntity<GenericResponse<Page<TransferResponse>>> getHistory(
            @RequestParam String userId,
            @RequestParam String startDate, 
            @RequestParam String endDate,
            Pageable pageable) { 
        
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
    @GetMapping("/fraud-report")
    public ResponseEntity<GenericResponse<List<Object[]>>> getFraudReport() {
        List<Object[]> suspiciousRecords = suspiciousTransfersHandler.handle();
        return ResponseEntity.ok(GenericResponse.success(suspiciousRecords));
    }
}