package com.project.transaction.infrastructure.mapper;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper responsible for data transformation between the Core Domain layer 
 * and the Infrastructure (Persistence) layer.
 * This ensures that the domain models remain completely isolated from database-specific 
 * annotations (e.g., JPA @Entity) and persistence details, strictly enforcing 
 * Clean Architecture and Hexagonal boundaries.
 */
@Mapper(componentModel = "spring")
public interface TransactionInfrastructureMapper {

    /**
     * Converts a core domain Wallet model into a JPA persistence entity.
     * Used before saving or updating a wallet's state in the database.
     *
     * @param model The domain model containing the active business state.
     * @return The mapped JPA entity ready for persistence.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdTraceId", ignore = true)
    @Mapping(target = "updatedTraceId", ignore = true)
    WalletEntity toWalletEntity(WalletModel model);

    /**
     * Converts a JPA persistence Wallet entity back into a core domain model.
     * Used after retrieving data from the database to reconstruct the Rich Domain Model.
     *
     * @param entity The data object retrieved from the database.
     * @return The domain model ready for business logic execution.
     */
    @Mapping(target = "isActive", expression = "java(entity.isActive())")
    WalletModel toWalletModel(WalletEntity entity);

    /**
     * Converts a core domain Transaction Record model into a JPA persistence entity.
     *
     * @param model The finalized transaction domain model resulting from a use case execution.
     * @return The mapped JPA entity to be saved in the transaction history table.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdTraceId", ignore = true)
    @Mapping(target = "updatedTraceId", ignore = true)
    TransactionRecordEntity toRecordEntity(TransactionRecordModel model);

    /**
     * Converts a JPA persistence Transaction Record entity back into a core domain model.
     * Used when fetching transaction history from the database to return to the application layer.
     *
     * @param entity The historical transaction record retrieved from the database.
     * @return The domain model representation of the transaction.
     */
    TransactionRecordModel toRecordModel(TransactionRecordEntity entity);
}
