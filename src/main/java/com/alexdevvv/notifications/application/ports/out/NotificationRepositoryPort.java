package com.alexdevvv.notifications.application.ports.out;

import com.alexdevvv.notifications.domain.model.TrackedNotification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia y cola de las notificaciones: la tabla funciona como outbox.
 */
public interface NotificationRepositoryPort {

    /**
     * Persiste la notificación. Si otra del mismo cliente ya usa su Idempotency-Key
     * (dos requests simultáneos), devuelve la existente en lugar de duplicarla.
     */
    TrackedNotification save(TrackedNotification notification);

    Optional<TrackedNotification> findByIdAndClientId(UUID id, String clientId);

    Optional<TrackedNotification> findByClientIdAndIdempotencyKey(String clientId, String idempotencyKey);

    /**
     * Reserva hasta {@code limit} notificaciones pendientes con el próximo intento vencido.
     * Instancias concurrentes nunca reciben la misma notificación.
     */
    List<TrackedNotification> claimDue(Instant now, int limit);
}
