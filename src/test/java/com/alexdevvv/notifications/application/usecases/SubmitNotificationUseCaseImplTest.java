package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.exceptions.IdempotencyKeyReusedException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitNotificationUseCaseImplTest {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");
    private static final String KEY = "order-42";

    @Mock
    private RecipientResolver recipientResolver;
    @Mock
    private NotificationRepositoryPort notificationRepository;

    private final Notification notification = new Notification("client-a", "user1", "Subject", "Body", null);

    @Test
    void submit_shouldStoreNotificationAsPending() {
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TrackedNotification tracked = useCase().submit(notification, null);

        assertThat(tracked.id()).isNotNull();
        assertThat(tracked.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(tracked.nextAttemptAt()).isEqualTo(NOW);
        assertThat(tracked.notification()).isEqualTo(notification);
    }

    @Test
    void submit_shouldRejectUndeliverableNotificationWithoutQueueingIt() {
        when(recipientResolver.resolve(notification)).thenThrow(new UserNotFoundException("client-a", "user1"));

        assertThrows(UserNotFoundException.class, () -> useCase().submit(notification, null));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void submit_shouldReturnOriginal_whenIdempotencyKeyIsReplayedWithSameRequest() {
        TrackedNotification original = TrackedNotification.pending(UUID.randomUUID(), notification, KEY, NOW);
        when(notificationRepository.findByClientIdAndIdempotencyKey("client-a", KEY)).thenReturn(Optional.of(original));

        TrackedNotification result = useCase().submit(notification, KEY);

        assertThat(result).isSameAs(original);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void submit_shouldReject_whenIdempotencyKeyIsReusedWithDifferentRequest() {
        TrackedNotification original = TrackedNotification.pending(UUID.randomUUID(), notification, KEY, NOW);
        when(notificationRepository.findByClientIdAndIdempotencyKey("client-a", KEY)).thenReturn(Optional.of(original));
        Notification different = new Notification("client-a", "user1", "Subject", "Another body", null);

        assertThrows(IdempotencyKeyReusedException.class, () -> useCase().submit(different, KEY));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void submit_shouldReturnWinner_whenConcurrentRequestWithSameKeyWasStoredFirst() {
        TrackedNotification winner = TrackedNotification.pending(UUID.randomUUID(), notification, KEY, NOW);
        when(notificationRepository.findByClientIdAndIdempotencyKey("client-a", KEY)).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(winner);

        TrackedNotification result = useCase().submit(notification, KEY);

        assertThat(result.id()).isEqualTo(winner.id());
    }

    private SubmitNotificationUseCaseImpl useCase() {
        return new SubmitNotificationUseCaseImpl(recipientResolver, notificationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
