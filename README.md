# Servicio de Notificaciones

Este proyecto implementa un servicio de notificaciones basado en Spring Boot 3 y Java 21, siguiendo los principios de Arquitectura Limpia (Hexagonal) y SOLID. Está diseñado para ser un módulo de notificaciones extensible que soporta envío por email, WhatsApp (simulado) y Push Notification (simulado), con la capacidad de escalar fácilmente a futuros métodos de notificación.

## Características Clave

*   **Arquitectura Limpia (Hexagonal):** Separación estricta de la lógica de negocio (dominio) de los detalles de infraestructura.
*   **Principio de Inversión de Dependencias (DIP):** El dominio es independiente de las implementaciones externas.
*   **Patrón Estrategia:** Facilita la adición de nuevos tipos de notificación sin modificar la lógica principal.
*   **Tipos de Notificación:**
    *   **Email:** Implementación real usando Spring Mail.
    *   **WhatsApp:** Infraestructura simulada (log) para mostrar la extensibilidad.
    *   **Push Notification:** Infraestructura simulada (log) para mostrar la extensibilidad.
*   **API REST:** Un endpoint HTTP para enviar solicitudes de notificación.
*   **Validación de Entrada:** Uso de `jakarta.validation` para asegurar la calidad de los datos de entrada.

## Tecnologías Utilizadas

*   **Java 21**
*   **Spring Boot 3.5.10**
*   **Maven**
*   **Lombok**
*   **JUnit 5 & Mockito**

## Construcción y Ejecución

Para construir y ejecutar este proyecto, sigue los siguientes pasos:

1.  **Navegar al directorio del proyecto:**
    ```bash
    cd notification-service
    ```

2.  **Construir el proyecto con Maven:**
    ```bash
    ./mvnw clean install
    ```
    *   Esto compilará el código, ejecutará las pruebas y empaquetará la aplicación en un archivo JAR.

3.  **Ejecutar las pruebas unitarias (opcional):**
    ```bash
    ./mvnw test
    ```
    *   Esto ejecutará solo las pruebas unitarias.

4.  **Ejecutar la aplicación Spring Boot:**
    ```bash
    ./mvnw spring-boot:run
    ```
    O, si ya has construido el JAR:
    ```bash
    java -jar target/notification-service-0.0.1-SNAPSHOT.jar
    ```

## Envío de Notificaciones (API REST)

Una vez que la aplicación esté ejecutándose, puedes enviar solicitudes de notificación a la API REST `/api/v1/notifications` (método `POST`) usando herramientas como `curl`.

### Notificación por Email:

```bash
curl -X POST http://localhost:8080/api/v1/notifications 
-H "Content-Type: application/json" 
-d '{
    "recipient": "destino@example.com",
    "subject": "Asunto del Email",
    "body": "Este es el cuerpo del mensaje de email.",
    "type": "EMAIL"
}'
```
*   **Nota:** Para que el envío real de correos funcione, necesitarás configurar un servidor SMTP válido en `src/main/resources/application.properties`. Si no, verás errores relacionados con la conexión SMTP, pero la lógica interna del infraestructura de email se activará.

### Notificación por WhatsApp (simulada):

```bash
curl -X POST http://localhost:8080/api/v1/notifications 
-H "Content-Type: application/json" 
-d '{
    "recipient": "+1234567890",
    "body": "Este es un mensaje de WhatsApp simulado.",
    "type": "WHATSAPP"
}'
```
*   Esto imprimirá un mensaje en la consola del servidor (infraestructura de log).

### Notificación Push (simulada):

```bash
curl -X POST http://localhost:8080/api/v1/notifications 
-H "Content-Type: application/json" 
-d '{
    "recipient": "token_del_dispositivo_xyz",
    "subject": "Título Push",
    "body": "Este es el cuerpo de la notificación push simulada.",
    "type": "PUSH"
}'
```
*   Esto imprimirá un mensaje en la consola del servidor (infraestructura de log).
