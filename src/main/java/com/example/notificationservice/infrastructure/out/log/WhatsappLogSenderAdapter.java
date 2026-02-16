package com.example.notificationservice.infrastructure.out.log;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationContent;
import com.example.notificationservice.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WhatsappLogSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappLogSenderAdapter.class);

    @Override
    public void send(String recipient, NotificationContent content) {
        logger.info("LOG (WhatsApp): Sending WhatsApp to {} with message: {}", recipient, content.body());
        // In a real application, this would integrate with a WhatsApp API (e.g., Twilio, Meta API)
    }

    @Override
    public NotificationType getType() {
        return NotificationType.WHATSAPP;
    }
}
