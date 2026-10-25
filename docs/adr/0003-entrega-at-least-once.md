# 0003. Entrega *at-least-once*

**Estado:** Aceptada

## Contexto

Entre "el proveedor aceptó el mensaje" y "la base registró `SENT`" siempre hay una ventana. Si el pod muere o la base no responde en ese momento, el servicio no tiene forma de saber si el envío ocurrió. Pasa lo mismo cuando el proveedor acepta el mensaje pero la respuesta no llega por un timeout.

Frente a esa duda hay dos opciones: reintentar, y arriesgar un duplicado, o no reintentar, y arriesgar una pérdida.

## Decisión

Ante la duda, reintentar. La notificación queda `PENDING` hasta que el envío se confirma en la base. Un fallo después del envío la deja pendiente, y el worker la vuelve a enviar.

## Consecuencias

**A favor:**

- **Ninguna notificación aceptada se pierde en silencio.** O termina en `SENT`, o en `FAILED` con `lastError` explicando por qué.

**En contra:**

- **Un usuario puede recibir la misma notificación dos veces.** Pasa en los casos de borde descritos, y en el escenario de [ADR 0004](0004-transaccion-abierta-durante-el-envio.md), donde lo amplifica el lote.
- **Los consumidores deben tolerar duplicados.** Esto queda documentado para los clientes: el `id` de la notificación es estable y sirve para deduplicar del lado del receptor.

## Por qué no *exactly-once*

No se puede garantizar de punta a punta sin que el proveedor colabore: haría falta que deduplique por una clave que le mandamos. El paso siguiente natural es propagar el `id` de la notificación como clave de idempotencia en los proveedores que lo soporten. No cambia el modelo del servicio.

Para notificaciones, un duplicado ocasional es mucho más barato que una pérdida silenciosa.

## Alternativas descartadas

- **At-most-once** (marcar `SENT` antes de enviar). Elimina los duplicados a costa de perder notificaciones ante cualquier fallo del proveedor, justo el caso que motivó el outbox.
