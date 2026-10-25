# 0007. Email por la API HTTP de Resend en lugar de SMTP

**Estado:** Aceptada

## Contexto

El servicio corre en DigitalOcean Kubernetes. DigitalOcean bloquea por defecto el tráfico SMTP saliente desde sus Droplets, y los nodos del cluster son Droplets. El adapter SMTP original no puede entregar en producción sin abrir un ticket de excepción.

## Decisión

- **Adapter HTTP.** `ResendEmailSenderAdapter` envía por la API HTTPS de Resend, usando `RestClient`.
- **Proveedor configurable.** Se elige con `MAIL_PROVIDER` (`smtp` por defecto, o `resend`). Los dos adapters son excluyentes por `@ConditionalOnProperty`, porque el mapa de estrategias admite un solo sender por tipo ([ADR 0001](0001-arquitectura-hexagonal.md)). Un test lo verifica.
- **SMTP sigue siendo el default** para desarrollo local, con Mailpit en `compose.yaml`.
- **Timeouts del cliente HTTP:** 5 s de conexión y 10 s de lectura. Son obligatorios porque el envío ocurre con el lote bloqueado ([ADR 0004](0004-transaccion-abierta-durante-el-envio.md)).

## Consecuencias

**A favor:**

- **Funciona en cualquier red que permita HTTPS saliente.**
- **Trazabilidad.** El log registra el id que devuelve Resend, lo que permite rastrear un envío en su panel.
- **El caso de uso no cambió.** El cambio de proveedor quedó contenido en un adapter.

**En contra:**

- **Dependencia de un proveedor externo**, con su cuota y su disponibilidad.
- **Todos los errores de Resend se reintentan como transitorios,** incluidos los `4xx` que no se arreglan reintentando. El tope de intentos acota el costo. Distinguirlos (`4xx` sin `429` como `undeliverable`) es una mejora pendiente.

## Alternativas descartadas

- **Pedir a DigitalOcean el desbloqueo de SMTP.** Depende de una aprobación manual y ata el servicio a una excepción del proveedor de nube.
- **Un relay SMTP en otro puerto.** Es una solución frágil frente a una API HTTP estándar.
