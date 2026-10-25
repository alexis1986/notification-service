# 0001. Arquitectura hexagonal con un adapter por canal

**Estado:** Aceptada

## Contexto

El servicio tiene que soportar varios canales (email, WhatsApp, push), y es de esperar que se sumen más. Cada canal tiene su propio proveedor, protocolo y modo de fallar. La lógica de negocio no depende de ningún proveedor: resolver a qué usuario, por qué canal y con qué dirección enviar, y decidir si un error se reintenta.

## Decisión

- **Arquitectura hexagonal.** El dominio (`domain`) y los casos de uso (`application`) no dependen de Spring ni de la infraestructura.
- **Puertos.** La aplicación define lo que necesita como interfaces, por ejemplo `NotificationSenderPort` y `NotificationRepositoryPort`, y la infraestructura las implementa.
- **Un adapter por canal.** Cada canal implementa `NotificationSenderPort` y declara qué `NotificationType` atiende. `RecipientResolver` arma un mapa `tipo → sender` con todos los beans disponibles.

## Consecuencias

**A favor:**

- **Sumar un canal no toca la lógica.** Basta un adapter nuevo y una fila en `user_addresses`; no se modifican `RecipientResolver` ni los casos de uso.
- **Tests de dominio rápidos.** Los casos de uso se prueban con Mockito, sin levantar Spring ni la base.
- **Proveedor intercambiable.** Cambiar de proveedor es reemplazar un adapter ([ADR 0007](0007-resend-por-http.md) lo hizo con el email).

**En contra:**

- **Más piezas que un CRUD en capas.** Hay que mapear entre entidades JPA y modelos de dominio, y escribir interfaces que hoy tienen una sola implementación.
- **Un sender por tipo.** El mapa se rompe si hay dos senders activos para el mismo tipo. Por eso los adapters de email son excluyentes por configuración.

## Alternativas descartadas

- **Capas clásicas con un `switch` por canal en el servicio.** Es más corto al principio, pero cada canal nuevo modifica la clase central y mezcla los detalles del proveedor con las reglas de negocio.
