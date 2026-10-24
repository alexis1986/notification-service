# Lineamientos de Desarrollo para el Servicio de Notificaciones

Este documento establece las convenciones y prácticas de desarrollo para el proyecto del Servicio de Notificaciones, garantizando consistencia, calidad y mantenibilidad del código.

## 1. Arquitectura y Principios

El proyecto sigue una **Arquitectura Limpia (Hexagonal)** y adhiere estrictamente a los principios **SOLID**.

*   **Dominio (Core de la Aplicación):** Contiene la lógica de negocio central, entidades y reglas (ej., `Notification`, `UserContactInfo`). Es la capa más interna e independiente.
*   **Puertos (Interfaces):** Definen los contratos para la comunicación del dominio con el exterior.
    *   **Puertos de Entrada (Driving Ports):** Interfaces que el dominio expone para recibir comandos o consultas (ej., `SubmitNotificationUseCase`, `DispatchNotificationsUseCase`).
    *   **Puertos de Salida (Driven Ports):** Interfaces que el dominio necesita para interactuar con la infraestructura (ej., `NotificationSenderPort`, `UserContactInfoRepository`).
*   **Infraestructura (Implementación):** Implementan los puertos para conectar el dominio con tecnologías específicas.
    *   **Adaptadores Primarios (Driving Adapters):** Implementan puertos de entrada (ej., `NotificationController` para REST, `ApiKeyAuthInterceptor` para seguridad, `NotificationDispatchScheduler` para el despacho).
    *   **Adaptadores Secundarios (Driven Adapters):** Implementan puertos de salida (ej., `EmailNotificationSenderAdapter`, `InMemoryUserContactInfoRepository`).

### Principios SOLID Aplicados:

*   **Single Responsibility Principle (SRP):** Cada clase tiene una única razón para cambiar (ej., un caso de uso se encarga de una única funcionalidad).
*   **Open/Closed Principle (OCP):** Las entidades de software deben estar abiertas a la extensión, pero cerradas a la modificación (ej., añadir un nuevo tipo de notificación no requiere cambiar `RecipientResolver` ni los casos de uso).
*   **Liskov Substitution Principle (LSP):** Los objetos de un programa deben ser reemplazables por instancias de sus subtipos sin alterar la corrección del programa.
*   **Interface Segregation Principle (ISP):** Los clientes no deben ser forzados a depender de interfaces que no utilizan.
*   **Dependency Inversion Principle (DIP):** Las dependencias del código fuente deben dirigirse hacia abstracciones (Puertos), no hacia detalles (Infraestructura).

## 2. Estructura de Paquetes

La estructura de paquetes refleja la Arquitectura Limpia:

```
src/main/java/com/alexdevvv/notifications/
├── infrastructure/
│   ├── in/               # Adaptadores primarios (ej. REST controllers)
│   │   ├── web/
│   │   ├── web/interceptor/ # Interceptores de Spring MVC
│   │   └── scheduler/    # Disparadores programados (despacho del outbox)
│   └── out/              # Adaptadores secundarios (ej. Implementaciones de envío, persistencia)
│       ├── email/
│       ├── log/
│       └── persistence/  # Implementaciones de repositorios
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
*   **Nomenclatura:** Utilizar nombres descriptivos para paquetes, clases, métodos y variables (ej. `SubmitNotificationUseCase`, `Notification`).
*   **Java Records:** Se prefiere el uso de `record` para los objetos de transferencia de datos (DTOs) y modelos inmutables, para reducir el boilerplate y promover la inmutabilidad.
*   **Comentarios:** Usar comentarios solo cuando la complejidad del código lo justifique el *porqué* se hace algo, no el *qué*.

## 4. Estrategia de Pruebas

*   **Test-Driven Development (TDD):** Se fomenta la práctica de escribir pruebas antes del código de producción.
*   **Pruebas Unitarias (Dominio y Aplicación):**
    *   Se centran en la lógica de negocio y los casos de uso.
    *   Aisladas del framework y la infraestructura.
    *   Uso de Mockito para simular dependencias (ej., `NotificationSenderPort`, `UserContactInfoRepository`).
    *   El objetivo es una alta cobertura de código en las capas `domain` y `application`.
*   **Pruebas de Integración (Infraestructura y End-to-End):**
    *   Verifican la interacción entre los componentes y con la infraestructura real.

## 5. Escalabilidad y Extensibilidad

El uso del patrón Estrategia, junto con la inyección de dependencias de Spring, hace que el sistema sea altamente extensible. Para añadir un nuevo tipo de notificación (por ejemplo, SMS), solo se requieren los siguientes pasos:

1.  **Añadir el nuevo tipo al enum `NotificationType`**.
2.  **Crear una nueva implementación de `NotificationSenderPort`** (ej., `SmsNotificationSenderAdapter`).
    *   Esta nueva clase debe estar en la capa de `infrastructure.out`.
    *   Debe implementar los métodos de la interfaz `NotificationSenderPort`:
        *   `send(String recipient, NotificationContent content)`: Contiene la lógica de envío específica para el nuevo tipo, recibiendo el destinatario ya resuelto y el contenido de la notificación.
        *   `getType()`: Debe devolver el `NotificationType` correspondiente que maneja esta implementación (ej., `return NotificationType.SMS;`).
3.  **Anotar la nueva clase con `@Component`**.
    *   Esto permite que Spring Boot la detecte y la inyecte automáticamente en el mapa de estrategias del caso de uso.
4.  **Registrar la dirección del usuario para el nuevo canal** en `UserContactInfo.addresses` (dato, no código).
    *   El caso de uso resuelve el destinatario con `UserContactInfo.addressFor(type)`; si el usuario no tiene dirección para el canal, lanza `MissingContactInfoException`.

Gracias a este diseño, **no se requiere modificar** `RecipientResolver`, los casos de uso ni el modelo `UserContactInfo` para soportar el nuevo tipo de notificación.

## 6. Flujo de Trabajo Git y Convenciones de Commits

Para mantener un historial de versiones claro y facilitar la trazabilidad de los cambios, el proyecto sigue un flujo de trabajo basado en ramas y la especificación de Conventional Commits.

### Flujo de Ramas (Branching Workflow)

*   **`main`**: Rama principal que siempre debe reflejar el código listo para producción. Las integraciones directas a `main` están prohibidas.
*   **`develop`**: Rama de integración donde se consolidan las funcionalidades desarrolladas. Las `feature branches` se fusionan aquí.
*   **`feat/<feature-name>`**: Ramas de desarrollo para nuevas funcionalidades. Deben crearse a partir de `develop` y fusionarse de nuevo a `develop` una vez completadas.
*   **`fix/<issue-description>`**: Ramas para correcciones de errores. Deben crearse a partir de `develop` (o `main` para hotfixes) y fusionarse a la rama correspondiente.
*   **`refactor/<description>`**: Ramas para refactorizaciones de código. Deben crearse a partir de `develop` y fusionarse de nuevo a `develop`.

### Convenciones de Commits (Conventional Commits)

Cada mensaje de commit debe seguir la especificación de Conventional Commits. La estructura básica es:

```
<tipo>(<ámbito>): <descripción>
```

**Tipos comunes:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.

**Ejemplos:**
*   `feat(users): Añade el registro de nuevos usuarios`
*   `fix(auth): Corrige el fallo de autenticación de Google`
*   `refactor(payment): Elimina Lombok y utiliza records de Java`
*   `docs(readme): Actualiza la sección de instalación`
