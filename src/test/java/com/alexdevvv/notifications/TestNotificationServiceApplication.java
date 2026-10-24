package com.alexdevvv.notifications;

import org.springframework.boot.SpringApplication;

/**
 * Levanta la app en local con un Postgres efímero en Docker: {@code ./mvnw spring-boot:test-run}.
 */
public class TestNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(NotificationServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
