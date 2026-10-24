package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaNotificationRepository;

    public NotificationRepositoryAdapter(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

    @Override
    public TrackedNotification save(TrackedNotification notification) {
        return jpaNotificationRepository.save(NotificationEntity.from(notification)).toDomain();
    }

    @Override
    public Optional<TrackedNotification> findByIdAndClientId(UUID id, String clientId) {
        return jpaNotificationRepository.findByIdAndClientId(id, clientId).map(NotificationEntity::toDomain);
    }

    @Override
    public List<TrackedNotification> claimDue(Instant now, int limit) {
        return jpaNotificationRepository.claimDue(now, limit).stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }
}
