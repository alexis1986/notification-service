package com.alexdevvv.notifications.domain.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String clientId, String userId) {
        super("User '%s' not found for client '%s'".formatted(userId, clientId));
    }
}
