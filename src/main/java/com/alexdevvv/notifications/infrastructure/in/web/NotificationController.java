package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.application.ports.in.GetNotificationUseCase;
import com.alexdevvv.notifications.application.ports.in.SubmitNotificationUseCase;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    // Lo setea ApiKeyAuthInterceptor después de validar la API Key
    private static final String CLIENT_ID = "clientId";

    private final SubmitNotificationUseCase submitNotificationUseCase;
    private final GetNotificationUseCase getNotificationUseCase;

    public NotificationController(SubmitNotificationUseCase submitNotificationUseCase, GetNotificationUseCase getNotificationUseCase) {
        this.submitNotificationUseCase = submitNotificationUseCase;
        this.getNotificationUseCase = getNotificationUseCase;
    }

    /**
     * Acepta la notificación y la encola: la entrega es asíncrona y su estado se consulta en la URL de Location.
     */
    @PostMapping
    public ResponseEntity<NotificationResponseDto> sendNotification(
            @RequestAttribute(CLIENT_ID) String clientId,
            @Valid @RequestBody SendNotificationRequestDto dto) {

        Notification notification = new Notification(
                clientId,
                dto.userId(),
                dto.subject(),
                dto.body(),
                dto.type()
        );

        TrackedNotification tracked = submitNotificationUseCase.submit(notification);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tracked.id())
                .toUri();
        return ResponseEntity.accepted().location(location).body(NotificationResponseDto.from(tracked));
    }

    @GetMapping("/{id}")
    public NotificationResponseDto getNotification(@RequestAttribute(CLIENT_ID) String clientId, @PathVariable UUID id) {
        return NotificationResponseDto.from(getNotificationUseCase.get(clientId, id));
    }
}
