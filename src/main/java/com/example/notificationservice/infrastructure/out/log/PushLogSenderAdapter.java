package com.example.notificationservice.infrastructure.out.log;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationContent;
import com.example.notificationservice.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushLogSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(PushLogSenderAdapter.class);

    @Override
    public void send(String recipient, NotificationContent content) {
        logger.info("LOG (Push): Sending Push Notification to {} with subject: {} and message: {}",
                recipient, content.subject(), content.body());
        // In a real application, this would integrate with a Push Notification service (e.g., Firebase Cloud Messaging, APNS)
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }
}
