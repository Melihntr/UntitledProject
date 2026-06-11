package com.project.notification.domain.usecase;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.RecordNotificationInput;
import com.project.notification.domain.model.NotificationResult;
import com.project.notification.domain.model.NotificationSaveResult;
import com.project.notification.domain.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordNotificationHandler {

    private final NotificationPort notificationPort;

    public NotificationResult recordNotification(RecordNotificationInput input) {
        log.info("notification.record.request eventId={} type={} sourceService={} referenceId={} recipientId={}",
                input.eventId(), input.type(), input.sourceService(), input.referenceId(), input.recipientId());

        try {
            return notificationPort.findByEventId(input.eventId())
                    .map(this::duplicateResult)
                    .orElseGet(() -> save(input));
        } catch (RuntimeException exception) {
            log.error("notification.record.failure eventId={} type={} sourceService={} referenceId={} recipientId={}",
                    input.eventId(), input.type(), input.sourceService(), input.referenceId(), input.recipientId(),
                    exception);
            throw exception;
        }
    }

    private NotificationResult save(RecordNotificationInput input) {
        NotificationSaveResult saveResult = notificationPort.save(NotificationModel.from(input));
        NotificationModel notification = saveResult.notification();

        if (saveResult.duplicate()) {
            log.info("notification.record.concurrent-duplicate eventId={} notificationId={} status={}",
                    notification.eventId(), notification.id(), notification.status());
        } else {
            log.info("notification.record.success eventId={} notificationId={} type={} referenceId={} recipientId={} status={}",
                    notification.eventId(), notification.id(), notification.type(), notification.referenceId(),
                    notification.recipientId(), notification.status());
        }
        return NotificationResult.from(notification, saveResult.duplicate());
    }

    private NotificationResult duplicateResult(NotificationModel notification) {
        log.info("notification.record.duplicate eventId={} notificationId={} status={}",
                notification.eventId(), notification.id(), notification.status());
        return NotificationResult.from(notification, true);
    }
}
