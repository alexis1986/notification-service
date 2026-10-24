package com.alexdevvv.notifications.domain.exceptions;

public class UserNotFoundException extends UndeliverableNotificationException {

    public UserNotFoundException(String clientId, String userId) {
        super("User '%s' not found for client '%s'".formatted(userId, clientId));
    }
}
