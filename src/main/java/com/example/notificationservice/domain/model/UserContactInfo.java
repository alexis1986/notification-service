package com.example.notificationservice.domain.model;

public record UserContactInfo(
        String userId,
        String email,
        String phoneNumber,
        String pushToken,
        NotificationType preferredChannel
) {
}
