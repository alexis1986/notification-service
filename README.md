# Servicio de Notificaciones

Este proyecto implementa un servicio de notificaciones basado en Spring Boot 3 y Java 21, siguiendo los principios de Arquitectura Limpia (Hexagonal) y SOLID. Está diseñado para ser un módulo de notificaciones extensible que soporta envío por email, WhatsApp (simulado) y Push Notification (simulado), con la capacidad de escalar fácilmente a futuros métodos de notificación.

## Características Clave

*   **Arquitectura Limpia (Hexagonal):** Separación estricta de la lógica de negocio (dominio) de los detalles de infraestructura.
*   **Notificaciones Centradas en el Usuario:** Las notificaciones se envían a un `userId`, y el servicio resuelve el canal y la dirección de destino según las preferencias del usuario.
*   **Canal Forzado:** Los clientes pueden anular las preferencias del usuario y forzar el envío a través de un canal específico.
*   **Principio de Inversión de Dependencias (DIP):** El dominio es independiente de las implementaciones externas.
*   **Patrón Estrategia:** Facilita la adición de nuevos tipos de notificación sin modificar la lógica principal.
*   **API REST:** Un endpoint HTTP para enviar solicitudes de notificación.
*   **Validación de Entrada:** Uso de `jakarta.validation` para asegurar la calidad de los datos de entrada.
*   **Soporte para Docker:** Incluye un `Dockerfile` multi-stage para una fácil contenerización y despliegue.

## Tecnologías Utilizadas

*   **Java 21 (Amazon Corretto)**
*   **Spring Boot 4.1**
*   **PostgreSQL 17 + Flyway**
*   **Maven**
*   **Docker**
*   **JUnit 5, Mockito & Testcontainers**

## Construcción y Ejecución

Los tests de integración y la ejecución local usan Testcontainers: solo hace falta Docker corriendo.

### Ejecución Local

1.  **Construir el proyecto y correr los tests:**
    ```bash
    ./mvnw clean verify
    ```
2.  **Levantar la aplicación con un PostgreSQL efímero en Docker:**
    ```bash
    ./mvnw spring-boot:test-run
    ```
    Para usar una base existente, ejecutar el jar con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.

### Configuración

Toda la configuración sensible se toma de variables de entorno. Los valores por defecto apuntan a un PostgreSQL local (`localhost:5432/notifications`) y a un SMTP local sin autenticación, por ejemplo [Mailpit](https://mailpit.axllent.org/) en el puerto 1025. El esquema lo crea Flyway al arrancar.

| Variable | Default | Descripción |
|---|---|---|
| `MAIL_HOST` | `localhost` | Host SMTP |
| `MAIL_PORT` | `1025` | Puerto SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vacío | Credenciales SMTP |
| `MAIL_SMTP_AUTH` | `false` | Habilita autenticación SMTP |
| `MAIL_SMTP_STARTTLS` | `false` | Habilita STARTTLS |
| `MAIL_FROM` | `no-reply@alexdevvv.com` | Remitente de los emails |
| `MAIL_PROVIDER` | `smtp` | `smtp` o `resend` (API HTTP de [Resend](https://resend.com), para entornos con el SMTP saliente bloqueado, como DigitalOcean) |
| `RESEND_API_KEY` | vacío | API Key de Resend (requerida con `MAIL_PROVIDER=resend`) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/notifications` | URL JDBC de PostgreSQL |
| `DB_USERNAME` / `DB_PASSWORD` | `notifications` / `notifications` | Credenciales de la base de datos |
| `FLYWAY_LOCATIONS` | `classpath:db/migration,classpath:db/demo` | Con `classpath:db/migration` solo se aplica el esquema, sin las API Keys de demo |

### Entorno Completo con Docker Compose

Levanta el servicio con el perfil `prod`, PostgreSQL y [Mailpit](https://mailpit.axllent.org/) (los emails enviados se ven en `http://localhost:8025`):

```bash
docker compose up --build
```

El perfil `prod` exige `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` sin defaults, no carga los datos de demo (el compose los habilita explícitamente con `FLYWAY_LOCATIONS`), respeta los headers `X-Forwarded-*` del Ingress y emite logs JSON en formato ECS.

### Ejecución con Docker

1.  **Construir la imagen de Docker:**
    ```bash
    docker build -t notification-service .
    ```
2.  **Ejecutar el contenedor:**
    ```bash
    docker run -p 8080:8080 notification-service
    ```

## Envío de Notificaciones (API REST)

Una vez que la aplicación esté ejecutándose, puedes enviar solicitudes de notificación a la API REST `/api/v1/notifications`.

Cada request debe incluir el header `X-API-Key`, que identifica al cliente (tenant). Sin una API Key válida el servicio responde `401`.

El contrato OpenAPI está en `http://localhost:8080/v3/api-docs` y la consola interactiva en `http://localhost:8080/swagger-ui.html` (botón *Authorize* para cargar la API Key).

El servicio tiene pre-configurados los siguientes datos de demo:

| API Key | Cliente | Usuario | Canal preferido |
|---|---|---|---|
| `key-for-client-a` | `client-a` | `user1` | `EMAIL` |
| `key-for-client-a` | `client-a` | `user2` | `WHATSAPP` |
| `super-secret-key-for-b` | `client-b` | `user1` | `PUSH` |

### Entrega Asíncrona y Consulta de Estado

El `POST` valida el request (usuario, canal y dirección) y encola la notificación en PostgreSQL (patrón outbox). Responde `202 Accepted` con el header `Location` y el estado inicial:

```json
{ "id": "6f1c…", "status": "PENDING", "attempts": 0, "lastError": null, "createdAt": "…", "updatedAt": "…" }
```

Un worker la despacha en segundo plano. Si el proveedor falla, reintenta con backoff exponencial y, al agotar los intentos, la marca `FAILED`, que funciona como dead-letter. Los errores que no se resuelven reintentando, como un usuario o una dirección inexistentes, pasan directo a `FAILED`. El estado se consulta con la URL de `Location`:

```bash
curl http://localhost:8080/api/v1/notifications/<id> -H "X-API-Key: key-for-client-a"
```

Cada cliente solo ve sus propias notificaciones: el `id` de otro cliente responde `404`.

### Reintentos Seguros con `Idempotency-Key`

Si el cliente no sabe si su request llegó (timeout, corte de red), puede reenviarlo con el mismo header `Idempotency-Key`. Recibe la notificación original y no se encola un duplicado. Reusar la clave con un contenido distinto responde `422`. Las claves son únicas por cliente.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-H "X-API-Key: key-for-client-a" \
-H "Idempotency-Key: order-42-shipped" \
-d '{ "userId": "user1", "subject": "Tu pedido salió", "body": "El pedido 42 está en camino." }'
```

| Variable | Default | Descripción |
|---|---|---|
| `DISPATCH_INTERVAL` | `PT2S` | Cada cuánto el worker busca notificaciones pendientes |
| `DISPATCH_BATCH_SIZE` | `20` | Notificaciones por lote |
| `DISPATCH_MAX_ATTEMPTS` | `5` | Intentos antes de marcar `FAILED` |
| `DISPATCH_INITIAL_BACKOFF` / `DISPATCH_MAX_BACKOFF` | `PT10S` / `PT10M` | Espera entre reintentos: se duplica en cada intento hasta el máximo |

### Notificación Usando Preferencia del Usuario

Para este ejemplo, `user2` tiene como preferencia `WHATSAPP`.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-H "X-API-Key: key-for-client-a" \
-d '{
    "userId": "user2",
    "subject": "Asunto Opcional",
    "body": "Este es el cuerpo del mensaje."
}'
```
*   El servicio buscará las preferencias de `user2` y enviará una notificación de WhatsApp simulada.

### Notificación Forzando un Canal

Aunque la preferencia de `user1` es `EMAIL`, el cliente puede forzar el envío a `PUSH`.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-H "X-API-Key: key-for-client-a" \
-d '{
    "userId": "user1",
    "subject": "Notificación Push",
    "body": "Este es un mensaje PUSH forzado.",
    "type": "PUSH"
}'
```
*   El servicio ignorará la preferencia del usuario y enviará una notificación Push simulada.

### Ejemplo de Envío de Email

El usuario `user1` tiene como preferencia `EMAIL`.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
-H "X-API-Key: key-for-client-a" \
-d '{
    "userId": "user1",
    "subject": "Email para user1",
    "body": "Este es el cuerpo del mensaje de email."
}'
```
*   **Nota:** Para que el envío de correos funcione necesitás un servidor SMTP accesible, configurado con las variables `MAIL_*` (ver [Configuración](#configuración)). Si no hay ninguno, la notificación se acepta igual. El worker la reintenta y termina en `FAILED`, con el error de conexión en `lastError`.
