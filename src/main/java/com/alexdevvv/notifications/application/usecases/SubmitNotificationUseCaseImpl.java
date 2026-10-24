package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.SubmitNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.time.Clock;
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
    public TrackedNotification submit(Notification notification) {
        // Falla rápido: lo que no se puede entregar se rechaza ahora y no ocupa la cola
        recipientResolver.resolve(notification);
        return notificationRepository.save(TrackedNotification.pending(UUID.randomUUID(), notification, clock.instant()));
    }
}
