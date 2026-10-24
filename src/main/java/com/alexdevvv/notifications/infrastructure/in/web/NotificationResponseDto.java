package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.domain.model.NotificationStatus;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponseDto(
        UUID id,
        NotificationStatus status,
        int attempts,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {

    static NotificationResponseDto from(TrackedNotification tracked) {
        return new NotificationResponseDto(
                tracked.id(),
                tracked.status(),
                tracked.attempts(),
                tracked.lastError(),
                tracked.createdAt(),
                tracked.updatedAt()
        );
    }
}
