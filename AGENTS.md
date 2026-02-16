# Lineamientos de Desarrollo para el Servicio de Notificaciones

Este documento establece las convenciones y prácticas de desarrollo para el proyecto del Servicio de Notificaciones, garantizando consistencia, calidad y mantenibilidad del código.

## 1. Arquitectura y Principios

El proyecto sigue una **Arquitectura Limpia (Hexagonal)** y adhiere estrictamente a los principios **SOLID**.

*   **Dominio (Core de la Aplicación):** Contiene la lógica de negocio central, entidades y reglas. Es la capa más interna e independiente.
*   **Puertos (Interfaces):** Definen los contratos para la comunicación del dominio con el exterior.
    *   **Puertos de Entrada (Driving Ports):** Interfaces que el dominio expone para recibir comandos o consultas (ej., `SendNotificationUseCase`).
    *   **Puertos de Salida (Driven Ports):** Interfaces que el dominio necesita para interactuar con la infraestructura (ej., `NotificationSenderPort`).
*   **Infraestructura (Implementación):** Implementan los puertos para conectar el dominio con tecnologías específicas.
    *   **Infraestructura Primarios (Driving Adapters):** Implementan puertos de entrada (ej., `NotificationController` para REST).
    *   **Infraestructura Secundarios (Driven Adapters):** Implementan puertos de salida (ej., `EmailNotificationSenderAdapter`, `WhatsappLogSenderAdapter`).

### Principios SOLID Aplicados:

*   **Single Responsibility Principle (SRP):** Cada clase tiene una única razón para cambiar (ej., un caso de uso se encarga de una única funcionalidad).
*   **Open/Closed Principle (OCP):** Las entidades de software deben estar abiertas a la extensión, pero cerradas a la modificación (ej., añadir un nuevo tipo de notificación no requiere cambiar el `SendNotificationUseCase`).
*   **Liskov Substitution Principle (LSP):** Los objetos de un programa deben ser reemplazables por instancias de sus subtipos sin alterar la corrección del programa.
*   **Interface Segregation Principle (ISP):** Los clientes no deben ser forzados a depender de interfaces que no utilizan.
*   **Dependency Inversion Principle (DIP):** Las dependencias del código fuente deben dirigirse hacia abstracciones (Puertos), no hacia detalles (Infraestructura).

## 2. Estructura de Paquetes

La estructura de paquetes refleja la Arquitectura Limpia:

```
src/main/java/com/example/notificationservice/
├── infrastructure/
│   ├── in/               # Infraestructura primarios (ej. REST controllers)
│   │   └── web/
│   └── out/              # Infraestructura secundarios (ej. Implementaciones de envío)
│       ├── email/
│       └── log/
├── application/          # Lógica de aplicación, casos de uso y orquestación
│   ├── ports/
│   │   ├── in/           # Puertos de entrada (interfaces de casos de uso)
│   │   └── out/          # Puertos de salida (interfaces requeridas por la aplicación)
│   └── usecases/         # Implementaciones de los casos de uso
├── config/               # Clases de configuración de Spring
├── domain/               # Core del negocio (independiente de cualquier framework)
│   ├── model/            # Entidades y objetos de valor
│   └── exceptions/       # Excepciones de dominio
└── NotificationServiceApplication.java # Clase principal de Spring Boot
```

## 3. Código

*   **Legibilidad:** El código debe ser claro, conciso y autoexplicativo.
*   **Nomenclatura:** Utilizar nombres descriptivos para paquetes, clases, métodos y variables (ej. `SendNotificationUseCase`, `NotificationRequest`).
*   **Comentarios:** Usar comentarios solo cuando la complejidad del código lo justifique el *porqué* se hace algo, no el *qué*.
*   **Lombok:** Se utiliza Lombok para reducir el boilerplate (ej. `@Value`, `@Builder`).

## 4. Estrategia de Pruebas

*   **Test-Driven Development (TDD):** Se fomenta la práctica de escribir pruebas antes del código de producción.
*   **Pruebas Unitarias (Dominio y Aplicación):**
    *   Se centran en la lógica de negocio y los casos de uso.
    *   Aisladas del framework y la infraestructura.
    *   Uso de Mockito para simular dependencias (ej., `NotificationSenderPort`).
    *   El objetivo es una alta cobertura de código en las capas `domain` y `application`.
*   **Pruebas de Integración (Infraestructura y End-to-End):**
    *   Verifican la interacción entre los componentes y con la infraestructura real (ej., controladores REST).
    *   No son el foco principal de este PoC, pero son una parte esencial de un sistema completo.

## 5. Escalabilidad y Extensibilidad

El uso del patrón Estrategia, junto con la inyección de dependencias de Spring, hace que el sistema sea altamente extensible. Para añadir un nuevo tipo de notificación (por ejemplo, SMS), solo se requieren los siguientes pasos:

1.  **Añadir el nuevo tipo al enum `NotificationType`**.
2.  **Crear una nueva implementación de `NotificationSenderPort`** (ej., `SmsNotificationSenderAdapter`).
    *   Esta nueva clase debe estar en la capa de `infrastructure.out`.
    *   Debe implementar los métodos de la interfaz `NotificationSenderPort`:
        *   `sendNotification(NotificationRequest request)`: Contiene la lógica de envío específica para el nuevo tipo.
        *   `getType()`: Debe devolver el `NotificationType` correspondiente que maneja esta implementación (ej., `return NotificationType.SMS;`).
3.  **Anotar la nueva clase con `@Component`**.
    *   Esto permite que Spring Boot la detecte y la inyecte automáticamente en el mapa de estrategias del caso de uso.

Gracias a este diseño, **no se requiere modificar** la lógica de orquestación en `SendNotificationUseCaseImpl` ni ninguna otra parte del core de la aplicación para soportar el nuevo tipo de notificación.
