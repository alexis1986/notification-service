package com.alexdevvv.notifications.domain.model;

public enum NotificationStatus {
    PENDING,
    SENT,
    /** Agotó los reintentos o es imposible de entregar: cumple el rol de dead-letter. */
    FAILED
}
