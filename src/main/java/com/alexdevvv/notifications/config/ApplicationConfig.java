package com.alexdevvv.notifications.config;

import com.alexdevvv.notifications.application.ports.in.SendNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.application.usecases.SendNotificationUseCaseImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ApplicationConfig {

    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            UserContactInfoRepository userContactInfoRepository,
            Set<NotificationSenderPort> notificationSenderPorts) {
        return new SendNotificationUseCaseImpl(userContactInfoRepository, notificationSenderPorts);
    }
}
