package com.project.transaction.infrastructure.entity;

import com.project.common.infrastructure.audit.AuditableEntity;
import com.project.user.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Persistence entity representing a transaction history record in the relational database.
 * This class resides strictly within the infrastructure layer and is used solely for 
 * Object-Relational Mapping (ORM) via Spring Data JPA / Hibernate. 
 * It is completely decoupled from the core business domain to enforce clean architecture.
 */
@Entity
@Table(name = "transaction_records")
@Getter
@Setter
@NoArgsConstructor
public class TransactionRecordEntity extends AuditableEntity {

    /**
     * The unique identifier of the user who initiated the transfer (sender).
     */
    @Column(name = "sender_user_id", nullable = false)
    private String senderUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sender_user_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_sender")
    )
    private UserEntity sender;

    /**
     * The unique identifier of the user who received the funds.
     */
    @Column(name = "receiver_user_id", nullable = false)
    private String receiverUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receiver_user_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_receiver")
    )
    private UserEntity receiver;

    /**
     * The monetary amount that was transferred.
     * * Enterprise Note: While Double is used in Java for simplicity here, standard 
     * financial database schemas map this column using columnDefinition = "DECIMAL(19,4)" 
     * to strictly prevent floating-point precision loss at the database level.
     */
    @Column(name = "amount", nullable = false)
    private Double amount;

    /**
     * The exact date and time when the transaction was successfully processed and recorded.
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /**
     * The final state of the transaction execution (e.g., "COMPLETED", "FAILED", "SUSPICIOUS").
     */
    @Column(name = "status", nullable = false)
    private String status;
}
