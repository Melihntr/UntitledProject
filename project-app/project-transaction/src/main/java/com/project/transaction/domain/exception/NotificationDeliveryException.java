package com.project.transaction.domain.exception;

import lombok.Getter;

@Getter
public class NotificationDeliveryException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;
    private final String traceId;

    public NotificationDeliveryException(
            String message,
            int httpStatus,
            String errorCode,
            String traceId,
            Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.traceId = traceId;
    }
}
