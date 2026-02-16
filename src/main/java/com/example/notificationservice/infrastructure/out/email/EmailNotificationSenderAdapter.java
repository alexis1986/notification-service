package com.example.notificationservice.infrastructure.out.email;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationContent;
import com.example.notificationservice.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSenderAdapter.class);

    private final JavaMailSender javaMailSender;

    public EmailNotificationSenderAdapter(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(String recipient, NotificationContent content) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipient);
        mailMessage.setSubject(content.subject());
        mailMessage.setText(content.body());
        // TODO: Configure sender email in application.properties or through another configuration mechanism
        // mailMessage.setFrom("your_email@example.com"); 

        javaMailSender.send(mailMessage);
        logger.info("Email sent to: {} with subject: {}", recipient, content.subject());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
