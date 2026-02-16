package com.example.notificationservice.infrastructure.in.web;

import com.example.notificationservice.domain.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequestDto(
        @NotBlank String recipient,
        String subject, // Optional for some notification types
        @NotBlank String body,
        @NotNull NotificationType type
) {
}
