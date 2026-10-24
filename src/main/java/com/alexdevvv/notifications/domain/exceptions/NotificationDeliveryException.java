package com.alexdevvv.notifications.domain.exceptions;

import com.alexdevvv.notifications.domain.model.NotificationType;

public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(NotificationType type, Throwable cause) {
        super("Delivery through channel %s failed".formatted(type), cause);
    }
}
