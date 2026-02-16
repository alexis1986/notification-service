package com.example.notificationservice.infrastructure.out.log;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WhatsappLogSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappLogSenderAdapter.class);

    @Override
    public void sendNotification(NotificationRequest request) {
        logger.info("LOG (WhatsApp): Sending WhatsApp to {} with message: {}", request.recipient(), request.body());
        // In a real application, this would integrate with a WhatsApp API (e.g., Twilio, Meta API)
    }

    @Override
    public NotificationType getType() {
        return NotificationType.WHATSAPP;
    }
}
