package com.example.notificationservice.config;

import com.example.notificationservice.application.ports.in.SendNotificationUseCase;
import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.application.usecases.SendNotificationUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ApplicationConfig {

    @Bean
    public SendNotificationUseCase sendNotificationUseCase(Set<NotificationSenderPort> notificationSenderPorts) {
        return new SendNotificationUseCaseImpl(notificationSenderPorts);
    }
}
