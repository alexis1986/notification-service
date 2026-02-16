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

## 6. Flujo de Trabajo Git y Convenciones de Commits

Para mantener un historial de versiones claro y facilitar la trazabilidad de los cambios, el proyecto sigue un flujo de trabajo basado en ramas y la especificación de Conventional Commits.

### Flujo de Ramas (Branching Workflow)

*   **`main`**: Rama principal que siempre debe reflejar el código listo para producción. Las integraciones directas a `main` están prohibidas.
*   **`develop`**: Rama de integración donde se consolidan las funcionalidades desarrolladas. Las `feature branches` se fusionan aquí.
*   **`feat/<feature-name>`**: Ramas de desarrollo para nuevas funcionalidades. Deben crearse a partir de `develop` y fusionarse de nuevo a `develop` una vez completadas.
*   **`fix/<issue-description>`**: Ramas para correcciones de errores. Deben crearse a partir de `develop` (o `main` para hotfixes) y fusionarse a la rama correspondiente.
*   **`refactor/<description>`**: Ramas para refactorizaciones de código. Deben crearse a partir de `develop` y fusionarse de nuevo a `develop`.

### Convenciones de Commits (Conventional Commits)

Cada mensaje de commit debe seguir la especificación de Conventional Commits, lo que permite generar historiales de cambios y versionado semántico automáticamente. La estructura básica es:

```
<tipo>(<ámbito>): <descripción>

[cuerpo opcional]

[pie de página opcional]
```

**Tipos (Types) comunes:**

*   **`feat`**: Una nueva funcionalidad.
*   **`fix`**: Una corrección de error.
*   **`docs`**: Cambios en la documentación.
*   **`style`**: Cambios que no afectan el significado del código (espacios en blanco, formato, puntos y comas omitidos, etc.).
*   **`refactor`**: Un cambio de código que no corrige un error ni añade una característica.
*   **`perf`**: Un cambio de código que mejora el rendimiento.
*   **`test`**: Añadir pruebas que faltan o corregir pruebas existentes.
*   **`build`**: Cambios que afectan el sistema de compilación o dependencias externas (ej., maven, npm).
*   **`ci`**: Cambios en los archivos y scripts de configuración de CI.
*   **`chore`**: Otros cambios que no modifican el código fuente o las pruebas.

**Ámbito (Scope) opcional:** Proporciona un contexto adicional para el cambio (ej., `(auth)`, `(api)`, `(ui)`, `(pom)`).

**Ejemplos:**

*   `feat(users): Añade el registro de nuevos usuarios`
*   `fix(auth): Corrige el fallo de autenticación de Google`
*   `refactor(payment): Elimina Lombok y utiliza records de Java`
*   `docs(readme): Actualiza la sección de instalación`
