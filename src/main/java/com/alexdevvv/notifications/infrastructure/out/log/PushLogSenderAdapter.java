package com.alexdevvv.notifications.infrastructure.out.log;

import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;
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
