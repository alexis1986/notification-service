package com.alexdevvv.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TrackedNotificationTest {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");
    private static final RetryPolicy POLICY = new RetryPolicy(3, Duration.ofSeconds(10), Duration.ofSeconds(15));

    private final TrackedNotification pending = TrackedNotification.pending(
            UUID.randomUUID(), new Notification("client-a", "user1", "Subject", "Body", null), NOW);

    @Test
    void pending_shouldBeDueImmediately() {
        assertThat(pending.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(pending.attempts()).isZero();
        assertThat(pending.nextAttemptAt()).isEqualTo(NOW);
    }

    @Test
    void sent_shouldCountTheAttempt() {
        TrackedNotification sent = pending.sent(NOW);

        assertThat(sent.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(sent.attempts()).isEqualTo(1);
    }

    @Test
    void failedAttempt_shouldRescheduleWithExponentialBackoffCappedAtMax() {
        TrackedNotification first = pending.failedAttempt("timeout", NOW, POLICY);
        TrackedNotification second = first.failedAttempt("timeout", NOW, POLICY);

        assertThat(first.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(first.nextAttemptAt()).isEqualTo(NOW.plusSeconds(10));
        assertThat(first.lastError()).isEqualTo("timeout");
        // 10s * 2 = 20s, acotado por maxBackoff
        assertThat(second.nextAttemptAt()).isEqualTo(NOW.plusSeconds(15));
    }

    @Test
    void failedAttempt_shouldGiveUpWhenAttemptsAreExhausted() {
        TrackedNotification exhausted = pending
                .failedAttempt("timeout", NOW, POLICY)
                .failedAttempt("timeout", NOW, POLICY)
                .failedAttempt("timeout", NOW, POLICY);

        assertThat(exhausted.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(exhausted.attempts()).isEqualTo(3);
    }

    @Test
    void undeliverable_shouldFailWithoutRetrying() {
        TrackedNotification failed = pending.undeliverable("no address", NOW);

        assertThat(failed.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(failed.attempts()).isEqualTo(1);
    }

    @Test
    void errors_shouldBeTruncatedToFitStorage() {
        TrackedNotification failed = pending.undeliverable("x".repeat(5000), NOW);

        assertThat(failed.lastError()).hasSize(TrackedNotification.MAX_ERROR_LENGTH);
    }
}
