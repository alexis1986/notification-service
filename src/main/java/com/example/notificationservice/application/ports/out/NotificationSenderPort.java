package com.example.notificationservice.application.ports.out;

import com.example.notificationservice.domain.model.NotificationContent;
import com.example.notificationservice.domain.model.NotificationType;

public interface NotificationSenderPort {
    void send(String recipient, NotificationContent content);
    NotificationType getType();
}
