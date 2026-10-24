package com.alexdevvv.notifications.domain.exceptions;

/**
 * Errores permanentes de entrega: reintentar no cambiaría el resultado,
 * así que la notificación pasa directo a FAILED.
 */
public abstract class UndeliverableNotificationException extends RuntimeException {

    protected UndeliverableNotificationException(String message) {
        super(message);
    }
}
