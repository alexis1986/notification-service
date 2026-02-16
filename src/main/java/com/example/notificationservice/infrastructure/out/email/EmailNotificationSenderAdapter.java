package com.example.notificationservice.infrastructure.out.email;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSenderAdapter implements NotificationSenderPort {

    private final JavaMailSender javaMailSender;

    public EmailNotificationSenderAdapter(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(request.recipient());
        mailMessage.setSubject(request.subject());
        mailMessage.setText(request.body());
        // TODO: Configure sender email in application.properties or through another configuration mechanism
        // mailMessage.setFrom("your_email@example.com"); 

        javaMailSender.send(mailMessage);
        System.out.println("Email sent to: " + request.recipient() + " with subject: " + request.subject());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
