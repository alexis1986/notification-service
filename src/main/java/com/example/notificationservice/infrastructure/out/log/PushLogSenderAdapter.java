package com.example.notificationservice.infrastructure.out.log;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushLogSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(PushLogSenderAdapter.class);

    @Override
    public void sendNotification(NotificationRequest request) {
        logger.info("LOG (Push): Sending Push Notification to {} with subject: {} and message: {}",
                request.recipient(), request.subject(), request.body());
        // In a real application, this would integrate with a Push Notification service (e.g., Firebase Cloud Messaging, APNS)
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }
}
