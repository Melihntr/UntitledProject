package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class TransactionRecordSpecifications {

    private TransactionRecordSpecifications() {
    }

    public static Specification<TransactionRecordEntity> historyForUser(
            String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("sender").get("id"), userId),
                        criteriaBuilder.equal(root.get("receiver").get("id"), userId)
                ),
                criteriaBuilder.between(root.get("transactionDate"), startDate, endDate)
        );
    }
}
