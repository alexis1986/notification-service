package com.alexdevvv.notifications.domain.exceptions;

import com.alexdevvv.notifications.domain.model.NotificationType;

public class MissingContactInfoException extends UndeliverableNotificationException {

    public MissingContactInfoException(String userId, NotificationType type) {
        super("User '%s' has no contact information for channel %s".formatted(userId, type));
    }
}
