package com.project.transaction.infrastructure.mapper;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionInfrastructureMapperTest {

    private final TransactionInfrastructureMapper mapper = Mappers.getMapper(TransactionInfrastructureMapper.class);

    @Test
    void toWalletEntity_mapsAllFields() {
        WalletModel model = WalletModel.builder()
                .id("w-1")
                .userId("user-1")
                .balance(123.45)
                .isActive(true)
                .version(5L)
                .build();

        WalletEntity entity = mapper.toWalletEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("w-1");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getBalance()).isEqualTo(123.45);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(5L);
    }

    @Test
    void toWalletModel_mapsAllFields() {
        WalletEntity entity = new WalletEntity();
        entity.setId("we-2");
        entity.setUserId("user-2");
        entity.setBalance(50.0);
        entity.setActive(true);
        entity.setVersion(2L);

        WalletModel model = mapper.toWalletModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo("we-2");
        assertThat(model.getUserId()).isEqualTo("user-2");
        assertThat(model.getBalance()).isEqualTo(50.0);
        assertThat(model.isActive()).isTrue();
        assertThat(model.getVersion()).isEqualTo(2L);
    }

    @Test
    void toRecordEntity_and_toRecordModel_mapAllFields() {
        LocalDateTime when = LocalDateTime.now();

        TransactionRecordModel model = TransactionRecordModel.builder()
                .id("tx-1")
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(10.0)
                .transactionDate(when)
                .status("COMPLETED")
                .build();

        TransactionRecordEntity entity = mapper.toRecordEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getSender()).isNull();
        assertThat(entity.getReceiver()).isNull();
        assertThat(entity.getSenderUserId()).isEqualTo("alice");
        assertThat(entity.getReceiverUserId()).isEqualTo("bob");
        assertThat(entity.getAmount()).isEqualTo(10.0);
        assertThat(entity.getTransactionDate()).isEqualTo(when);
        assertThat(entity.getStatus()).isEqualTo("COMPLETED");

        // And back
        TransactionRecordModel mappedBack = mapper.toRecordModel(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isNull();
        assertThat(mappedBack.getSenderUserId()).isEqualTo("alice");
        assertThat(mappedBack.getReceiverUserId()).isEqualTo("bob");
        assertThat(mappedBack.getAmount()).isEqualTo(10.0);
        assertThat(mappedBack.getTransactionDate()).isEqualTo(when);
        assertThat(mappedBack.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void mapping_null_returnsNull() {
        assertThat(mapper.toWalletEntity(null)).isNull();
        assertThat(mapper.toWalletModel(null)).isNull();
        assertThat(mapper.toRecordEntity(null)).isNull();
        assertThat(mapper.toRecordModel(null)).isNull();
    }
}
