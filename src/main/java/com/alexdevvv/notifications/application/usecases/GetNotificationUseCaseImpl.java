package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.GetNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.exceptions.NotificationNotFoundException;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.util.UUID;

public class GetNotificationUseCaseImpl implements GetNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public GetNotificationUseCaseImpl(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public TrackedNotification get(String clientId, UUID id) {
        // Filtrar por cliente evita que un tenant consulte notificaciones de otro
        return notificationRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
