package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.domain.exceptions.NotificationDeliveryException;
import com.alexdevvv.notifications.domain.exceptions.UserNotFoundException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationStatus;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.RetryPolicy;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchNotificationsUseCaseImplTest {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");
    private static final RetryPolicy POLICY = new RetryPolicy(3, Duration.ofSeconds(10), Duration.ofMinutes(10));
    private static final int BATCH_SIZE = 10;

    @Mock
    private RecipientResolver recipientResolver;
    @Mock
    private NotificationRepositoryPort notificationRepository;
    @Mock
    private NotificationSenderPort sender;

    private DispatchNotificationsUseCaseImpl dispatchNotificationsUseCase;

    @BeforeEach
    void setUp() {
        dispatchNotificationsUseCase = new DispatchNotificationsUseCaseImpl(
                recipientResolver, notificationRepository, POLICY, BATCH_SIZE, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void dispatchDue_shouldSendAndMarkAsSent() {
        TrackedNotification tracked = pending("user1");
        givenDue(tracked);
        givenDeliverable(tracked);

        int processed = dispatchNotificationsUseCase.dispatchDue();

        assertThat(processed).isEqualTo(1);
        verify(sender).send("user1@client-a.com", new NotificationContent("Subject", "Body"));
        assertThat(savedStatuses()).containsExactly(NotificationStatus.SENT);
    }

    @Test
    void dispatchDue_shouldRescheduleWhenProviderFails() {
        TrackedNotification tracked = pending("user1");
        givenDue(tracked);
        givenDeliverable(tracked);
        doThrow(new NotificationDeliveryException(NotificationType.EMAIL, new RuntimeException("SMTP down")))
                .when(sender).send(any(), any());

        dispatchNotificationsUseCase.dispatchDue();

        TrackedNotification saved = savedNotifications().getFirst();
        assertThat(saved.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.attempts()).isEqualTo(1);
        assertThat(saved.nextAttemptAt()).isEqualTo(NOW.plusSeconds(10));
    }

    @Test
    void dispatchDue_shouldFailWithoutRetry_whenNotificationIsUndeliverable() {
        TrackedNotification tracked = pending("gone");
        givenDue(tracked);
        when(recipientResolver.resolve(tracked.notification())).thenThrow(new UserNotFoundException("client-a", "gone"));

        dispatchNotificationsUseCase.dispatchDue();

        assertThat(savedStatuses()).containsExactly(NotificationStatus.FAILED);
    }

    @Test
    void dispatchDue_shouldKeepProcessingTheBatch_whenOneNotificationFails() {
        TrackedNotification failing = pending("gone");
        TrackedNotification ok = pending("user1");
        givenDue(failing, ok);
        when(recipientResolver.resolve(failing.notification())).thenThrow(new UserNotFoundException("client-a", "gone"));
        givenDeliverable(ok);

        int processed = dispatchNotificationsUseCase.dispatchDue();

        assertThat(processed).isEqualTo(2);
        assertThat(savedStatuses()).containsExactly(NotificationStatus.FAILED, NotificationStatus.SENT);
    }

    private static TrackedNotification pending(String userId) {
        return TrackedNotification.pending(UUID.randomUUID(), new Notification("client-a", userId, "Subject", "Body", null), NOW);
    }

    private void givenDue(TrackedNotification... notifications) {
        when(notificationRepository.claimDue(NOW, BATCH_SIZE)).thenReturn(List.of(notifications));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenDeliverable(TrackedNotification tracked) {
        when(recipientResolver.resolve(tracked.notification()))
                .thenReturn(new RecipientResolver.Delivery(sender, tracked.notification().userId() + "@client-a.com"));
    }

    private List<TrackedNotification> savedNotifications() {
        ArgumentCaptor<TrackedNotification> captor = ArgumentCaptor.forClass(TrackedNotification.class);
        verify(notificationRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }

    private List<NotificationStatus> savedStatuses() {
        return savedNotifications().stream().map(TrackedNotification::status).toList();
    }
}
