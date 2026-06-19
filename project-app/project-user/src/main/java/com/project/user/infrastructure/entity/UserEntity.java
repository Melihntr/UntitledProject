package com.project.user.infrastructure.entity;

import com.project.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Database Entity representing the "users" table.
 * This class is isolated within the infrastructure layer.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "is_user_deleted", nullable = false)
    private boolean isUserDeleted = false;

    /**
     * Used for Optimistic Locking to handle concurrent transactions safely.
     */
    @Version
    @Column(name = "version")
    private Long version;
}
