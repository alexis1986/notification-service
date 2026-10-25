# 0005. `Idempotency-Key` por cliente, sin expiración

**Estado:** Aceptada

## Contexto

Un cliente que hace `POST` y recibe un timeout no sabe si la notificación se encoló. Si reintenta a ciegas, el usuario recibe dos avisos. Hace falta una forma segura de reintentar.

## Decisión

- **Header opcional.** El cliente puede mandar un `Idempotency-Key` y la clave se guarda en la fila de la notificación.
- **Único por cliente.** Un índice parcial `(client_id, idempotency_key) WHERE idempotency_key IS NOT NULL` lo garantiza: dos clientes pueden usar el mismo valor sin pisarse.
- **Mismo request, misma respuesta.** Si llega una clave ya usada con el mismo contenido, se devuelve la notificación original y no se encola otra.
- **Clave reusada con otro contenido.** Responde `422`: es un error del cliente, no un reintento.
- **Carrera resuelta en la base.** Si dos requests iguales llegan juntos, el índice único hace fallar el segundo `INSERT`, que atrapa la violación y devuelve el registro ganador. Lo prueba un test de concurrencia.
- **Las claves no expiran.**

## Consecuencias

**A favor:**

- **No hay tabla aparte ni job de limpieza.** La clave vive y muere con su notificación.
- **Sin ventana arbitraria.** Un reintento tardío, por ejemplo de una cola del cliente que se recuperó días después, no se convierte en un duplicado.

**En contra:**

- **Reusar una clave para otro mensaje es un error.** Un cliente que "recicla" claves no puede mandar un mensaje distinto con una clave vieja: recibe `422`.
- **El índice crece con la tabla `notifications`.** El crecimiento está acotado por la retención de notificaciones. Cuando se implemente una purga de notificaciones viejas, las claves expiran con ellas, sin lógica adicional.

## Alternativas descartadas

- **TTL de 24 h en una tabla dedicada**, al estilo de los procesadores de pago. Suma una tabla, un job de limpieza y una ventana que el cliente tiene que conocer, sin un beneficio real a este volumen.
