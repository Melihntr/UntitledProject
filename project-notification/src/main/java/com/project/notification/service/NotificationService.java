package com.project.notification.service;

import com.project.notification.dto.NotificationRequest;
import com.project.notification.dto.NotificationResponse;
import com.project.notification.entity.NotificationEntity;
import com.project.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse recordNotification(NotificationRequest request) {
        log.info("notification.record.request eventId={} type={} sourceService={} referenceId={} recipientId={}",
                request.eventId(), request.type(), request.sourceService(), request.referenceId(), request.recipientId());

        return notificationRepository.findByEventId(request.eventId())
                .map(existing -> {
                    log.info("notification.record.duplicate eventId={} notificationId={} status={}",
                            existing.getEventId(), existing.getId(), existing.getStatus());
                    return toResponse(existing, true);
                })
                .orElseGet(() -> saveNewNotification(request));
    }

    private NotificationResponse saveNewNotification(NotificationRequest request) {
        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setEventId(request.eventId());
            notification.setType(request.type());
            notification.setSourceService(request.sourceService());
            notification.setRecipientId(request.recipientId());
            notification.setTitle(request.title());
            notification.setMessage(request.message());
            notification.setReferenceId(request.referenceId());
            notification.setAmount(request.amount());
            notification.setCurrency(request.currency());

            NotificationEntity saved = notificationRepository.save(notification);
            log.info("notification.record.success eventId={} notificationId={} type={} referenceId={} recipientId={} status={}",
                    saved.getEventId(), saved.getId(), saved.getType(), saved.getReferenceId(),
                    saved.getRecipientId(), saved.getStatus());
            return toResponse(saved, false);
        } catch (DataIntegrityViolationException exception) {
            return notificationRepository.findByEventId(request.eventId())
                    .map(existing -> {
                        log.info("notification.record.concurrent-duplicate eventId={} notificationId={} status={}",
                                existing.getEventId(), existing.getId(), existing.getStatus());
                        return toResponse(existing, true);
                    })
                    .orElseThrow(() -> {
                        log.error("notification.record.persistence-failure eventId={} type={} referenceId={} recipientId={}",
                                request.eventId(), request.type(), request.referenceId(), request.recipientId(), exception);
                        return exception;
                    });
        } catch (RuntimeException exception) {
            log.error("notification.record.failure eventId={} type={} sourceService={} referenceId={} recipientId={}",
                    request.eventId(), request.type(), request.sourceService(), request.referenceId(),
                    request.recipientId(), exception);
            throw exception;
        }
    }

    private NotificationResponse toResponse(NotificationEntity notification, boolean duplicate) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getStatus(),
                duplicate,
                notification.getCreatedAt()
        );
    }
}
