package com.example.notificationservice.infrastructure.in.web;

import com.example.notificationservice.application.ports.in.SendNotificationUseCase;
import com.example.notificationservice.domain.model.NotificationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody SendNotificationRequestDto dto) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .recipient(dto.getRecipient())
                .subject(dto.getSubject())
                .body(dto.getBody())
                .type(dto.getType())
                .build();

        sendNotificationUseCase.sendNotification(notificationRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
