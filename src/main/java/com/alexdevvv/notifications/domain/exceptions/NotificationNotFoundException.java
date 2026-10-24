package com.alexdevvv.notifications.domain.exceptions;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID id) {
        super("Notification '%s' not found".formatted(id));
    }
}
