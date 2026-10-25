# 0008. Demo pública con API Keys conocidas

**Estado:** Aceptada

## Contexto

El servicio es una pieza de portafolio: tiene que poder probarse desde Swagger UI sin pedir credenciales. Publicar API Keys en un servicio que envía emails reales tiene dos riesgos:

- que se use para mandar spam;
- que se agote la cuota del proveedor y se dañe la reputación del remitente.

## Decisión

- **Las API Keys de demo son públicas**, y están en el README y en el seed `db/demo`.
- **El cliente no elige destinatarios.** Indica un `userId`, y la dirección sale de la base. Con las keys de demo solo se puede escribir a los usuarios del seed, así que el servicio no sirve como relay de spam.
- **Las direcciones del seed son de prueba de Resend** (`delivered+…@resend.dev`). Simulan una entrega exitosa sin llegar a un buzón real, y sin rebotes que dañen la reputación del remitente.
- **El Ingress limita a 30 requests por minuto por IP** (`nginx.ingress.kubernetes.io/limit-rpm`), lo que protege la cuota de Resend.
- **El perfil `prod` no carga los datos de demo por defecto.** El chart los habilita de forma explícita con `FLYWAY_LOCATIONS`, como una decisión visible del despliegue.
- **Swagger UI y `/v3/api-docs` quedan expuestos en `prod` a propósito.** Así está declarado en `application-prod.properties`.

## Consecuencias

**A favor:**

- **Cualquiera puede probar el flujo completo**: aceptación, despacho, reintentos y consulta de estado, contra infraestructura real.
- **El daño posible está acotado por diseño**, no por la discreción de quien conozca las keys.

**En contra:**

- **Los emails no llegan a una persona.** Eso es deliberado; el estado `SENT` y el id de Resend en los logs demuestran la entrega.
- **Un uso abusivo puede degradar la demo** dentro del límite de requests. Es aceptable para un entorno de demostración.

## Para un uso real

Desplegar sin `db/demo`, dar de alta las API Keys por cliente y verificar un dominio propio en Resend para el remitente.
