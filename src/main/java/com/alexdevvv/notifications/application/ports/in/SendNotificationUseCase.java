package com.alexdevvv.notifications.application.ports.in;

import com.alexdevvv.notifications.domain.model.Notification;

public interface SendNotificationUseCase {
    void sendNotification(Notification notification);
}
