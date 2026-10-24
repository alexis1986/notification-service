package com.alexdevvv.notifications.application.ports.in;

import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.util.UUID;

public interface GetNotificationUseCase {
    TrackedNotification get(String clientId, UUID id);
}
