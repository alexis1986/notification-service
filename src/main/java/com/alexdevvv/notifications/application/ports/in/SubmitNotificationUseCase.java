package com.alexdevvv.notifications.application.ports.in;

import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

public interface SubmitNotificationUseCase {
    /**
     * @param idempotencyKey opcional; si el cliente ya envió esta clave con el mismo contenido,
     *                       se devuelve la notificación original sin encolar otra
     */
    TrackedNotification submit(Notification notification, String idempotencyKey);
}
