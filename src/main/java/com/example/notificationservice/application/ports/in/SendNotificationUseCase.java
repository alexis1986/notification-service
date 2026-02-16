package com.example.notificationservice.application.ports.in;

import com.example.notificationservice.domain.model.NotificationRequest;

public interface SendNotificationUseCase {
    void sendNotification(NotificationRequest command);
}
