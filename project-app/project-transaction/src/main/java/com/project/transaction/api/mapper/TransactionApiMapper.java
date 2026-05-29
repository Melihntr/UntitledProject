package com.project.transaction.api.mapper;

import com.project.transaction.api.dto.TransferRequest;
import com.project.transaction.api.dto.TransferResponse;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for API layer to convert DTOs to Domain inputs and vice versa.
 */
@Mapper(componentModel = "spring")
public interface TransactionApiMapper {

    // Request DTO -> Domain Input
    TransactionInput toInput(TransferRequest request);

    // Domain Model -> Response DTO
    @Mapping(target = "transactionId", source = "id")
    TransferResponse toResponse(TransactionRecordModel model);
}