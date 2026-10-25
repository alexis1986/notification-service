# 0004. El envío ocurre dentro de la transacción del lote

**Estado:** Aceptada

## Contexto

El worker tiene que garantizar que dos réplicas no envíen la misma notificación al mismo tiempo, y que una notificación tomada por un pod que muere vuelva a estar disponible.

Hay dos formas típicas de lograrlo:

- **Lease:** marcar la fila `IN_PROGRESS` con un vencimiento en una transacción corta, enviar fuera de la transacción y confirmar en otra.
- **Lock de fila:** mantener el lock (`FOR UPDATE SKIP LOCKED`) mientras se envía.

## Decisión

Lock de fila. `NotificationDispatchScheduler` ejecuta cada ciclo dentro de un `TransactionTemplate`, y en esa única transacción el worker:

1. toma el lote;
2. envía cada notificación;
3. guarda el resultado de cada una.

## Consecuencias

**A favor:**

- **Recuperación automática.** Si el pod muere, PostgreSQL libera los locks y las filas vuelven a estar disponibles. No hace falta un estado intermedio, ni vencimientos, ni un proceso que rescate leases huérfanos.
- **Las réplicas no se pisan.** `SKIP LOCKED` hace que cada worker salte las filas que tiene otro.

**En contra:**

- **La conexión queda tomada mientras se habla con el proveedor.** Con lotes de 20 y timeouts de 5 s (conexión) y 10 s (lectura), un proveedor colgado retiene el lote varios minutos en el peor caso. Por eso los timeouts del cliente HTTP son obligatorios (`spring.http.clients.*`), y el tamaño de lote es chico.
- **Reenvíos en bloque.** Si el pod muere a mitad del lote, la transacción hace rollback y las notificaciones ya enviadas de ese lote se vuelven a enviar. Es una fuente concreta de duplicados, aceptada en [ADR 0003](0003-entrega-at-least-once.md).
- **Un envío lento demora al resto del lote.**

## Cuándo revisarla

Si el volumen crece o los proveedores son lentos, conviene pasar a una transacción por notificación. Si eso no alcanza, al modelo de lease, para no sostener conexiones durante la llamada HTTP. El cambio queda contenido en el worker y el repositorio.

## Alternativas descartadas

- **Lease con `IN_PROGRESS` y `locked_until`.** Libera la conexión durante el envío, pero suma un estado, un vencimiento a calibrar y un proceso de rescate. Es complejidad que hoy no se justifica.
