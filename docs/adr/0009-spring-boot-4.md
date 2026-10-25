# 0009. Migración a Spring Boot 4

**Estado:** Aceptada

## Contexto

El servicio estaba en Spring Boot 3.5, cuyo soporte OSS terminaba el 30 de junio de 2026. El escaneo de dependencias reportaba dos vulnerabilidades CRITICAL en `spring-webmvc` 6.2 sin parche en la línea OSS.

## Decisión

Migrar a Spring Boot 4.1.2 (Spring Framework 7, Jakarta EE 11, Jackson 3) antes de sumar funcionalidad. Así el trabajo posterior (persistencia, outbox, Actuator) se escribió directamente sobre la nueva base.

Cambios que exigió la migración:

- **Starters modulares.** `spring-boot-starter-webmvc` y `spring-boot-starter-restclient`, con sus starters de test. Las anotaciones de test cambiaron de paquete, como `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
- **`HttpStatus.UNPROCESSABLE_CONTENT`** en lugar de `UNPROCESSABLE_ENTITY`, que quedó deprecado.
- **Testcontainers 2**, con los módulos renombrados (`testcontainers-postgresql`).
- **Timeouts de clientes HTTP** con `spring.http.clients.*`.

## Consecuencias

**A favor:**

- **Desaparecieron las vulnerabilidades.** El escaneo de dependencias y el de la imagen quedaron en 0.
- **Ventana de soporte OSS amplia** por delante.
- **Funcionalidades de la versión nueva.** Se usan los logs estructurados nativos (`logging.structured.format.console=ecs`) y el `RestClient.Builder` autoconfigurado.

**En contra:**

- **Algunas librerías del ecosistema van detrás de la línea 4**, y hubo que elegir versiones compatibles, como springdoc 3.x.
- **Menos material de referencia** en la comunidad que para la línea 3.

## Alternativas descartadas

- **Quedarse en 3.5 con soporte comercial.** No tiene sentido para un proyecto de portafolio y posterga una migración inevitable.
- **Forzar versiones parcheadas de las dependencias vulnerables dentro de 3.5.** Se intentó primero, pero no existía parche OSS para `spring-webmvc` 6.2.
