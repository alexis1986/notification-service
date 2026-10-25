# 0002. Outbox en PostgreSQL en lugar de un broker de mensajes

**Estado:** Aceptada

## Contexto

La primera versión enviaba la notificación dentro del request HTTP. Si el proveedor estaba caído, la notificación se perdía y el cliente recibía un error que no podía resolver ([ADR 0006](0006-202-en-lugar-de-502.md)).

Hacía falta desacoplar aceptar de entregar, con tres requisitos:

- persistir antes de responder;
- reintentar;
- dejar el estado consultable.

El servicio corre en un cluster chico, compartido con otros proyectos. Cada componente extra es memoria, operación y superficie de fallo.

## Decisión

Usar la tabla `notifications` de PostgreSQL como outbox:

- **Aceptación.** El `POST` hace un `INSERT` con `status = PENDING` y responde `202`.
- **Despacho.** Un worker `@Scheduled` toma lotes con `SELECT … FOR UPDATE SKIP LOCKED`, los envía y actualiza el estado.
- **Estado.** `PENDING → SENT | FAILED`. Los reintentos se programan con `next_attempt_at`, y un índice parcial sobre los `PENDING` mantiene barata la consulta del worker.

## Consecuencias

**A favor:**

- **Un solo componente con estado.** La misma base guarda usuarios, API Keys y la cola.
- **Aceptar y validar la `Idempotency-Key` es una única transacción.** No hay *dual write* entre una base y un broker.
- **El estado se consulta con un `SELECT`.** El `GET /notifications/{id}` y la dead-letter (`FAILED`) salen gratis.
- **Escala horizontal sin coordinación.** `SKIP LOCKED` reparte lotes disjuntos entre réplicas.

**En contra:**

- **Latencia de polling.** Una notificación espera hasta un intervalo (2 s por defecto) antes de salir.
- **Throughput acotado** por el polling y el tamaño de lote. Alcanza para notificaciones transaccionales, no para campañas masivas.
- **Consultas constantes.** La base recibe una consulta por intervalo aunque no haya trabajo; el índice parcial la hace barata.

## Cuándo revisarla

Si el volumen pasa a cientos de notificaciones por segundo, o si otros servicios necesitan consumir los eventos, conviene un broker (RabbitMQ o Kafka) alimentado desde esta misma outbox. Esa evolución no cambia la API.

## Alternativas descartadas

- **RabbitMQ o Kafka desde el inicio.** Resuelven un problema de escala que no existe y suman un componente con estado al cluster. Además, publicar después de commitear en la base reintroduce el problema del *dual write*.
- **`@Async` en memoria.** Pierde las notificaciones si el pod se reinicia.
