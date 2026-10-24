package com.alexdevvv.notifications.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Notificación aceptada por el servicio junto con el estado de su entrega.
 * Las transiciones devuelven una copia nueva: el registro es inmutable.
 *
 * @param idempotencyKey clave enviada por el cliente para que un reintento suyo no duplique la notificación; opcional
 */
public record TrackedNotification(
        UUID id,
        Notification notification,
        String idempotencyKey,
        NotificationStatus status,
        int attempts,
        Instant nextAttemptAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {

    public static final int MAX_ERROR_LENGTH = 1000;

    public static TrackedNotification pending(UUID id, Notification notification, Instant now) {
        return pending(id, notification, null, now);
    }

    public static TrackedNotification pending(UUID id, Notification notification, String idempotencyKey, Instant now) {
        return new TrackedNotification(id, notification, idempotencyKey, NotificationStatus.PENDING, 0, now, null, now, now);
    }

    public TrackedNotification sent(Instant now) {
        return transition(NotificationStatus.SENT, now, null, now);
    }

    /** Falla transitoria: se reprograma con backoff hasta agotar los intentos. */
    public TrackedNotification failedAttempt(String error, Instant now, RetryPolicy retryPolicy) {
        if (retryPolicy.isExhausted(attempts + 1)) {
            return transition(NotificationStatus.FAILED, now, error, now);
        }
        return transition(NotificationStatus.PENDING, now.plus(retryPolicy.backoffAfter(attempts + 1)), error, now);
    }

    /** Falla permanente: reintentar no cambiaría el resultado. */
    public TrackedNotification undeliverable(String error, Instant now) {
        return transition(NotificationStatus.FAILED, now, error, now);
    }

    private TrackedNotification transition(NotificationStatus newStatus, Instant nextAttempt, String error, Instant now) {
        return new TrackedNotification(id, notification, idempotencyKey, newStatus, attempts + 1, nextAttempt, truncate(error), createdAt, now);
    }

    private static String truncate(String error) {
        return error == null || error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}
