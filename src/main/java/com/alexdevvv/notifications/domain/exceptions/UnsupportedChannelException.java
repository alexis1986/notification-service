package com.alexdevvv.notifications.domain.exceptions;

import com.alexdevvv.notifications.domain.model.NotificationType;

public class UnsupportedChannelException extends UndeliverableNotificationException {

    public UnsupportedChannelException(NotificationType type) {
        super("No sender available for channel %s".formatted(type));
    }
}
