ALTER TABLE notifications ADD COLUMN idempotency_key VARCHAR(255);

-- La clave es única por cliente: dos clientes pueden usar el mismo valor sin pisarse.
-- Este índice es el que resuelve la carrera entre dos requests simultáneos con la misma clave.
CREATE UNIQUE INDEX uq_notifications_idempotency_key
    ON notifications (client_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
