package com.example.notificationservice.infrastructure.in.web;

import com.example.notificationservice.domain.model.NotificationType;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Value
public class SendNotificationRequestDto {
    @NotBlank
    String recipient;
    String subject; // Optional for some notification types
    @NotBlank
    String body;
    @NotNull
    NotificationType type;
}
