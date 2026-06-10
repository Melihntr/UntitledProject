package com.project.notification.repository;

import com.project.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for managing NotificationEntity persistence.
 * This interface abstracts the database interactions for the Notification Microservice,
 * providing built-in CRUD operations and the ability to execute custom queries 
 * against the independent notification database without requiring boilerplate SQL.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    Optional<NotificationEntity> findByEventId(String eventId);
}
