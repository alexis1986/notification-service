package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.SubmitNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.exceptions.IdempotencyKeyReusedException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class SubmitNotificationUseCaseImpl implements SubmitNotificationUseCase {

    private final RecipientResolver recipientResolver;
    private final NotificationRepositoryPort notificationRepository;
    private final Clock clock;

    public SubmitNotificationUseCaseImpl(RecipientResolver recipientResolver, NotificationRepositoryPort notificationRepository, Clock clock) {
        this.recipientResolver = recipientResolver;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Override
    public TrackedNotification submit(Notification notification, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<TrackedNotification> previous = notificationRepository.findByClientIdAndIdempotencyKey(notification.clientId(), idempotencyKey);
            if (previous.isPresent()) {
                return sameRequestOrFail(previous.get(), notification);
            }
        }

        // Falla rápido: lo que no se puede entregar se rechaza ahora y no ocupa la cola
        recipientResolver.resolve(notification);
        TrackedNotification saved = notificationRepository.save(
                TrackedNotification.pending(UUID.randomUUID(), notification, idempotencyKey, clock.instant()));
        // Si un request simultáneo con la misma clave ganó la carrera, saved es el suyo
        return sameRequestOrFail(saved, notification);
    }

    private static TrackedNotification sameRequestOrFail(TrackedNotification existing, Notification requested) {
        if (!existing.notification().equals(requested)) {
            throw new IdempotencyKeyReusedException(existing.idempotencyKey());
        }
        return existing;
    }
}
