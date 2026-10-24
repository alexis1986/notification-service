package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationStatus;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "forced_type", length = 20)
    private NotificationType forcedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = TrackedNotification.MAX_ERROR_LENGTH)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationEntity() {
    }

    static NotificationEntity from(TrackedNotification tracked) {
        NotificationEntity entity = new NotificationEntity();
        Notification notification = tracked.notification();
        entity.id = tracked.id();
        entity.clientId = notification.clientId();
        entity.userId = notification.userId();
        entity.subject = notification.subject();
        entity.body = notification.body();
        entity.forcedType = notification.forcedType();
        entity.status = tracked.status();
        entity.attempts = tracked.attempts();
        entity.nextAttemptAt = tracked.nextAttemptAt();
        entity.lastError = tracked.lastError();
        entity.createdAt = tracked.createdAt();
        entity.updatedAt = tracked.updatedAt();
        return entity;
    }

    TrackedNotification toDomain() {
        return new TrackedNotification(
                id,
                new Notification(clientId, userId, subject, body, forcedType),
                status,
                attempts,
                nextAttemptAt,
                lastError,
                createdAt,
                updatedAt
        );
    }
}
