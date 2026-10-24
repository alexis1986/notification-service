package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simula dos réplicas del worker reservando lotes al mismo tiempo:
 * FOR UPDATE SKIP LOCKED tiene que repartir las notificaciones sin duplicados ni bloqueos.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NotificationClaimConcurrencyTest {

    @Autowired
    private NotificationRepositoryPort notificationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanOutbox() {
        jdbcTemplate.update("DELETE FROM notifications");
    }

    @Test
    void concurrentWorkers_claimDisjointBatches() throws Exception {
        Instant now = Instant.now();
        List<UUID> all = IntStream.range(0, 4)
                .mapToObj(i -> notificationRepository.save(TrackedNotification.pending(
                        UUID.randomUUID(), new Notification("client-a", "user1", "Subject", "Body " + i, null), now.minusSeconds(1))).id())
                .toList();

        CountDownLatch firstClaimed = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // El primer worker reserva 2 y mantiene la transacción abierta
            Future<List<UUID>> firstWorker = executor.submit(() -> transactionTemplate.execute(status -> {
                List<UUID> claimed = ids(notificationRepository.claimDue(now, 2));
                firstClaimed.countDown();
                await(releaseFirst);
                return claimed;
            }));
            assertThat(firstClaimed.await(10, TimeUnit.SECONDS)).isTrue();

            // El segundo pide todo lo vencido: solo puede recibir lo que el primero no bloqueó
            List<UUID> secondWorker = transactionTemplate.execute(status -> ids(notificationRepository.claimDue(now, 10)));
            releaseFirst.countDown();

            List<UUID> first = firstWorker.get(10, TimeUnit.SECONDS);
            assertThat(first).hasSize(2);
            assertThat(secondWorker).hasSize(2).doesNotContainAnyElementsOf(first);
            assertThat(Stream.concat(first.stream(), secondWorker.stream()).toList()).containsExactlyInAnyOrderElementsOf(all);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentRequestsWithSameIdempotencyKey_storeASingleNotification() {
        Notification notification = new Notification("client-a", "user1", "Subject", "Body", null);
        Instant now = Instant.now();
        // Ambos requests pasaron el chequeo previo antes de que el otro insertara: decide el índice único
        TrackedNotification winner = notificationRepository.save(TrackedNotification.pending(UUID.randomUUID(), notification, "order-42", now));
        TrackedNotification loser = notificationRepository.save(TrackedNotification.pending(UUID.randomUUID(), notification, "order-42", now));

        assertThat(loser.id()).isEqualTo(winner.id());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM notifications", Integer.class)).isEqualTo(1);
    }

    private static List<UUID> ids(List<TrackedNotification> notifications) {
        return notifications.stream().map(TrackedNotification::id).toList();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
