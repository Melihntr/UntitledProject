package com.project.transaction.infrastructure.api.mapper;

import com.project.transaction.infrastructure.api.dto.TransferRequest;
import com.project.transaction.infrastructure.api.dto.TransferResponse;
import com.project.transaction.domain.usecase.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionApiMapperTest {

    private final TransactionApiMapper mapper = Mappers.getMapper(TransactionApiMapper.class);

    @Test
    void toInput_mapsFieldsCorrectly() {
        TransferRequest req = new TransferRequest();
        req.setSenderUserId("alice");
        req.setReceiverUserId("bob");
        req.setAmount(12.34);

        TransactionInput input = mapper.toInput(req);

        assertThat(input).isNotNull();
        assertThat(input.getSenderUserId()).isEqualTo("alice");
        assertThat(input.getReceiverUserId()).isEqualTo("bob");
        assertThat(input.getAmount()).isEqualTo(12.34);
    }

    @Test
    void toResponse_mapsIdAndOtherFields() {
        LocalDateTime now = LocalDateTime.now();
        TransactionRecordModel model = TransactionRecordModel.builder()
                .id("tx-123")
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(99.99)
                .transactionDate(now)
                .status("COMPLETED")
                .build();

        TransferResponse dto = mapper.toResponse(model);

        assertThat(dto).isNotNull();
        // id -> transactionId
        assertThat(dto.getTransactionId()).isEqualTo("tx-123");
        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
        assertThat(dto.getAmount()).isEqualTo(99.99);
        assertThat(dto.getTransactionDate()).isEqualTo(now);
    }

    @Test
    void mapping_nullSource_returnsNullForToInputAndToResponse() {
        assertThat(mapper.toInput(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }
}
