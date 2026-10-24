package com.alexdevvv.notifications.application.ports.in;

public interface DispatchNotificationsUseCase {
    /**
     * Intenta entregar un lote de notificaciones pendientes cuyo próximo intento ya venció.
     * Debe ejecutarse dentro de una transacción: los registros quedan bloqueados mientras se procesan.
     *
     * @return cantidad de notificaciones procesadas
     */
    int dispatchDue();
}
