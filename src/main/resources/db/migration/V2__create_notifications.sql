-- Outbox: cada notificación aceptada queda acá hasta que se entrega (SENT) o se descarta (FAILED)
CREATE TABLE notifications (
    id              UUID          PRIMARY KEY,
    client_id       VARCHAR(255)  NOT NULL,
    user_id         VARCHAR(255)  NOT NULL,
    subject         VARCHAR(255),
    body            TEXT          NOT NULL,
    forced_type     VARCHAR(20),
    status          VARCHAR(20)   NOT NULL,
    attempts        INT           NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ   NOT NULL,
    last_error      VARCHAR(1000),
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL
);

-- El worker solo busca pendientes vencidas: índice parcial chico aunque la tabla crezca con históricos
CREATE INDEX idx_notifications_due ON notifications (next_attempt_at) WHERE status = 'PENDING';
