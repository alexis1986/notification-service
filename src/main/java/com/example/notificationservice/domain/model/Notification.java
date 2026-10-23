package com.example.notificationservice.domain.model;

public record Notification(
        String clientId,
        String userId,
        String subject,
        String body,
        NotificationType forcedType
) {
}
