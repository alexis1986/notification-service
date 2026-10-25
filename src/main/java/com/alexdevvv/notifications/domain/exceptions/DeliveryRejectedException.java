package com.alexdevvv.notifications.domain.exceptions;

import com.alexdevvv.notifications.domain.model.NotificationType;

/**
 * El proveedor rechazó el contenido de la notificación (ej. dirección inválida):
 * reenviar lo mismo daría el mismo rechazo.
 */
public class DeliveryRejectedException extends UndeliverableNotificationException {

    public DeliveryRejectedException(NotificationType type, int status, String reason) {
        super("Provider rejected the %s notification (HTTP %d): %s".formatted(type, status, reason));
    }
}
