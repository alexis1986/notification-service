package com.alexdevvv.notifications.infrastructure.in.web.interceptor;

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException(String message) {
        super(message);
    }
}
