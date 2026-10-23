package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.domain.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequestDto(
        @NotBlank String userId,
        String subject,
        @NotBlank String body,
        NotificationType type // Optional: forces a specific channel
) {
}
