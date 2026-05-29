package com.project.transaction.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class TransactionRecordEntity {

    /**
     * The primary key of the transaction record.
     * This ID is generated in the domain layer (usually as a UUID) and passed down to persistence.
     */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * The unique identifier of the user who initiated the transfer (sender).
     */
    @Column(name = "sender_user_id", nullable = false)
    private String senderUserId;

    /**
     * The unique identifier of the user who received the funds.
     */
    @Column(name = "receiver_user_id", nullable = false)
    private String receiverUserId;

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