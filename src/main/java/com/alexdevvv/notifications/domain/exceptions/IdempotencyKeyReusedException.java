package com.alexdevvv.notifications.domain.exceptions;

public class IdempotencyKeyReusedException extends RuntimeException {

    public IdempotencyKeyReusedException(String idempotencyKey) {
        super("Idempotency-Key '%s' was already used with a different request".formatted(idempotencyKey));
    }
}
