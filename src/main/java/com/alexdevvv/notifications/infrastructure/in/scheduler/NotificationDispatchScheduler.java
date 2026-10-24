package com.alexdevvv.notifications.infrastructure.in.scheduler;

import com.alexdevvv.notifications.application.ports.in.DispatchNotificationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Adaptador primario que dispara el despacho del outbox.
 * La transacción mantiene bloqueado el lote reservado (FOR UPDATE SKIP LOCKED) hasta registrar el resultado:
 * si el proceso muere a mitad del lote, el rollback devuelve las notificaciones a la cola (entrega at-least-once).
 */
@Component
public class NotificationDispatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatchScheduler.class);

    private final DispatchNotificationsUseCase dispatchNotificationsUseCase;
    private final TransactionTemplate transactionTemplate;

    public NotificationDispatchScheduler(DispatchNotificationsUseCase dispatchNotificationsUseCase, TransactionTemplate transactionTemplate) {
        this.dispatchNotificationsUseCase = dispatchNotificationsUseCase;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(initialDelayString = "${notifications.dispatch.interval}", fixedDelayString = "${notifications.dispatch.interval}")
    public void dispatch() {
        Integer processed = transactionTemplate.execute(status -> dispatchNotificationsUseCase.dispatchDue());
        if (processed != null && processed > 0) {
            logger.info("Dispatched {} notifications", processed);
        }
    }
}
