package com.example.notificationservice.domain.model;

public record NotificationRequest(
        String recipient,
        String subject,
        String body,
        NotificationType type
) {
}
