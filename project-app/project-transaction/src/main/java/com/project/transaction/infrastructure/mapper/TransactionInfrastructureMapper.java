package com.project.transaction.infrastructure.mapper;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionInfrastructureMapper {

    WalletEntity toWalletEntity(WalletModel model);
    WalletModel toWalletModel(WalletEntity entity);

    TransactionRecordEntity toRecordEntity(TransactionRecordModel model);
    TransactionRecordModel toRecordModel(TransactionRecordEntity entity);
}