package com.alexdevvv.notifications.application.ports.out;

import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;

public interface NotificationSenderPort {
    void send(String recipient, NotificationContent content);
    NotificationType getType();
}
