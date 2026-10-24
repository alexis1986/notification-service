package com.alexdevvv.notifications.infrastructure.out.email;

import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.domain.exceptions.NotificationDeliveryException;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSenderAdapter.class);

    private final JavaMailSender javaMailSender;
    private final String from;

    public EmailNotificationSenderAdapter(JavaMailSender javaMailSender,
                                          @Value("${notifications.mail.from}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void send(String recipient, NotificationContent content) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(recipient);
        mailMessage.setSubject(content.subject());
        mailMessage.setText(content.body());

        try {
            javaMailSender.send(mailMessage);
        } catch (MailException e) {
            throw new NotificationDeliveryException(NotificationType.EMAIL, e);
        }
        logger.info("Email sent to: {} with subject: {}", recipient, content.subject());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
