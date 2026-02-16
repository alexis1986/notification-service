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
*   **Spring Boot 3.5.10**
*   **Maven**
*   **Docker**
*   **JUnit 5 & Mockito**

## Construcción y Ejecución

### Ejecución Local

1.  **Construir el proyecto con Maven:**
    ```bash
    ./mvnw clean install
    ```
2.  **Ejecutar la aplicación Spring Boot:**
    ```bash
    java -jar target/notification-service-0.0.1-SNAPSHOT.jar
    ```

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

El servicio tiene pre-configurados 3 usuarios (`user1`, `user2`, `user3`) con diferentes preferencias de canal.

### Notificación Usando Preferencia del Usuario

Para este ejemplo, `user2` tiene como preferencia `WHATSAPP`.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Content-Type: application/json" \
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
-d '{
    "userId": "user1",
    "subject": "Email para user1",
    "body": "Este es el cuerpo del mensaje de email."
}'
```
*   **Nota:** Para que el envío real de correos funcione, necesitarás configurar un servidor SMTP válido en `src/main/resources/application.properties`. Si no, verás un error 500, pero la traza en los logs del servicio mostrará que se intentó la conexión SMTP, validando que la lógica funcionó.
