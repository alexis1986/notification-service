package com.example.notificationservice.application.ports.out;

import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;

public interface NotificationSenderPort {
    void sendNotification(NotificationRequest request);
    NotificationType getType();
}
