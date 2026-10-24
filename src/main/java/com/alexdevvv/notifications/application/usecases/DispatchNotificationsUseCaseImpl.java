package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.DispatchNotificationsUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.exceptions.UndeliverableNotificationException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.RetryPolicy;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.time.Clock;
import java.util.List;

public class DispatchNotificationsUseCaseImpl implements DispatchNotificationsUseCase {

    private final RecipientResolver recipientResolver;
    private final NotificationRepositoryPort notificationRepository;
    private final RetryPolicy retryPolicy;
    private final int batchSize;
    private final Clock clock;

    public DispatchNotificationsUseCaseImpl(RecipientResolver recipientResolver, NotificationRepositoryPort notificationRepository,
                                            RetryPolicy retryPolicy, int batchSize, Clock clock) {
        this.recipientResolver = recipientResolver;
        this.notificationRepository = notificationRepository;
        this.retryPolicy = retryPolicy;
        this.batchSize = batchSize;
        this.clock = clock;
    }

    @Override
    public int dispatchDue() {
        List<TrackedNotification> due = notificationRepository.claimDue(clock.instant(), batchSize);
        due.forEach(notification -> notificationRepository.save(attempt(notification)));
        return due.size();
    }

    private TrackedNotification attempt(TrackedNotification tracked) {
        Notification notification = tracked.notification();
        try {
            // Se resuelve de nuevo: los datos de contacto pueden haber cambiado desde que se aceptó
            RecipientResolver.Delivery delivery = recipientResolver.resolve(notification);
            delivery.sender().send(delivery.recipient(), new NotificationContent(notification.subject(), notification.body()));
            return tracked.sent(clock.instant());
        } catch (UndeliverableNotificationException e) {
            return tracked.undeliverable(e.getMessage(), clock.instant());
        } catch (RuntimeException e) {
            // Cualquier otro error se considera transitorio: falla del proveedor, timeout, etc.
            return tracked.failedAttempt(e.getMessage(), clock.instant(), retryPolicy);
        }
    }
}
