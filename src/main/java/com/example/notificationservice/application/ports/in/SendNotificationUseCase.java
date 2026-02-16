package com.example.notificationservice.application.ports.in;

import com.example.notificationservice.domain.model.Notification;

public interface SendNotificationUseCase {
    void sendNotification(Notification notification);
}
