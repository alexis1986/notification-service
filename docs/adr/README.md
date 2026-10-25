# Architecture Decision Records

Decisiones de arquitectura con sus trade-offs. Cada ADR registra el contexto en el que se tomó la decisión, para poder revisarla cuando ese contexto cambie.

| ADR | Decisión | Estado |
|---|---|---|
| [0001](0001-arquitectura-hexagonal.md) | Arquitectura hexagonal con un adapter por canal | Aceptada |
| [0002](0002-outbox-en-postgresql.md) | Outbox en PostgreSQL en lugar de un broker de mensajes | Aceptada |
| [0003](0003-entrega-at-least-once.md) | Entrega *at-least-once* | Aceptada |
| [0004](0004-transaccion-abierta-durante-el-envio.md) | El envío ocurre dentro de la transacción del lote | Aceptada |
| [0005](0005-idempotency-key-sin-ttl.md) | `Idempotency-Key` por cliente, sin expiración | Aceptada |
| [0006](0006-202-en-lugar-de-502.md) | `202 Accepted` siempre; los fallos del proveedor no llegan al cliente | Aceptada |
| [0007](0007-resend-por-http.md) | Email por la API HTTP de Resend en lugar de SMTP | Aceptada |
| [0008](0008-demo-publica.md) | Demo pública con API Keys conocidas | Aceptada |
| [0009](0009-spring-boot-4.md) | Migración a Spring Boot 4 | Aceptada |

Formato: Contexto → Decisión → Consecuencias → Alternativas descartadas.
