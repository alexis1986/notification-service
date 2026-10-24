package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.exceptions.UserNotFoundException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationStatus;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitNotificationUseCaseImplTest {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");

    @Mock
    private RecipientResolver recipientResolver;
    @Mock
    private NotificationRepositoryPort notificationRepository;

    private final Notification notification = new Notification("client-a", "user1", "Subject", "Body", null);

    @Test
    void submit_shouldStoreNotificationAsPending() {
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TrackedNotification tracked = useCase().submit(notification);

        assertThat(tracked.id()).isNotNull();
        assertThat(tracked.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(tracked.nextAttemptAt()).isEqualTo(NOW);
        assertThat(tracked.notification()).isEqualTo(notification);
    }

    @Test
    void submit_shouldRejectUndeliverableNotificationWithoutQueueingIt() {
        when(recipientResolver.resolve(notification)).thenThrow(new UserNotFoundException("client-a", "user1"));

        assertThrows(UserNotFoundException.class, () -> useCase().submit(notification));
        verify(notificationRepository, never()).save(any());
    }

    private SubmitNotificationUseCaseImpl useCase() {
        return new SubmitNotificationUseCaseImpl(recipientResolver, notificationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
