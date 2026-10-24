package com.alexdevvv.notifications.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Notificación aceptada por el servicio junto con el estado de su entrega.
 * Las transiciones devuelven una copia nueva: el registro es inmutable.
 */
public record TrackedNotification(
        UUID id,
        Notification notification,
        NotificationStatus status,
        int attempts,
        Instant nextAttemptAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {

    public static final int MAX_ERROR_LENGTH = 1000;

    public static TrackedNotification pending(UUID id, Notification notification, Instant now) {
        return new TrackedNotification(id, notification, NotificationStatus.PENDING, 0, now, null, now, now);
    }

    public TrackedNotification sent(Instant now) {
        return new TrackedNotification(id, notification, NotificationStatus.SENT, attempts + 1, now, null, createdAt, now);
    }

    /** Falla transitoria: se reprograma con backoff hasta agotar los intentos. */
    public TrackedNotification failedAttempt(String error, Instant now, RetryPolicy retryPolicy) {
        int attempt = attempts + 1;
        if (retryPolicy.isExhausted(attempt)) {
            return new TrackedNotification(id, notification, NotificationStatus.FAILED, attempt, now, truncate(error), createdAt, now);
        }
        return new TrackedNotification(id, notification, NotificationStatus.PENDING, attempt,
                now.plus(retryPolicy.backoffAfter(attempt)), truncate(error), createdAt, now);
    }

    /** Falla permanente: reintentar no cambiaría el resultado. */
    public TrackedNotification undeliverable(String error, Instant now) {
        return new TrackedNotification(id, notification, NotificationStatus.FAILED, attempts + 1, now, truncate(error), createdAt, now);
    }

    private static String truncate(String error) {
        return error == null || error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}
