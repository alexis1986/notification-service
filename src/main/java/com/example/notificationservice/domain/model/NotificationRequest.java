package com.example.notificationservice.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationRequest {
    String recipient;
    String subject;
    String body;
    NotificationType type;
}
