package com.project.notification.infrastructure.adapter;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.NotificationSaveResult;
import com.project.notification.domain.port.NotificationPort;
import com.project.notification.infrastructure.entity.NotificationEntity;
import com.project.notification.infrastructure.mapper.NotificationInfrastructureMapper;
import com.project.notification.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPort {

    private final NotificationRepository notificationRepository;
    private final NotificationInfrastructureMapper notificationInfrastructureMapper;

    @Override
    public Optional<NotificationModel> findByEventId(String eventId) {
        return notificationRepository.findByEventId(eventId)
                .map(notificationInfrastructureMapper::toModel);
    }

    @Override
    public NotificationSaveResult save(NotificationModel notification) {
        try {
            NotificationEntity saved = notificationRepository.save(notificationInfrastructureMapper.toEntity(notification));
            return new NotificationSaveResult(notificationInfrastructureMapper.toModel(saved), false);
        } catch (DataIntegrityViolationException exception) {
            return findByEventId(notification.eventId())
                    .map(existing -> new NotificationSaveResult(existing, true))
                    .orElseThrow(() -> exception);
        }
    }
}
