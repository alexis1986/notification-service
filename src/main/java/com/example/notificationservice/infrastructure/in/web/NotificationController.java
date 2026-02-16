package com.example.notificationservice.infrastructure.in.web;

import com.example.notificationservice.application.ports.in.SendNotificationUseCase;
import com.example.notificationservice.domain.model.Notification;
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
        Notification notification = new Notification(
                dto.userId(),
                dto.subject(),
                dto.body(),
                dto.type()
        );

        sendNotificationUseCase.sendNotification(notification);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
