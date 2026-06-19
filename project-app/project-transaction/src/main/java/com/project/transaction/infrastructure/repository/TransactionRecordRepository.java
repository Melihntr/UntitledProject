package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for managing {@link TransactionRecordEntity} persistence.
 * This infrastructure component handles both standard CRUD operations and complex 
 * analytical queries (utilizing various SQL JOIN strategies) for reporting and fraud detection.
 * * Enterprise Note: Queries joining multiple entities (like UserEntity and WalletEntity) 
 * assume these tables reside in the same physical database and JPA persistence context.
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecordEntity, String>,
        JpaSpecificationExecutor<TransactionRecordEntity> {

    List<TransactionRecordEntity> findAllByStatus(String status);

    List<TransactionRecordEntity> findAllByAmountGreaterThan(Double amount);
}
