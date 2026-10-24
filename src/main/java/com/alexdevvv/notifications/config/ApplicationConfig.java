package com.alexdevvv.notifications.config;

import com.alexdevvv.notifications.application.ports.in.DispatchNotificationsUseCase;
import com.alexdevvv.notifications.application.ports.in.GetNotificationUseCase;
import com.alexdevvv.notifications.application.ports.in.SubmitNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationRepositoryPort;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.application.usecases.DispatchNotificationsUseCaseImpl;
import com.alexdevvv.notifications.application.usecases.GetNotificationUseCaseImpl;
import com.alexdevvv.notifications.application.usecases.RecipientResolver;
import com.alexdevvv.notifications.application.usecases.SubmitNotificationUseCaseImpl;
import com.alexdevvv.notifications.domain.model.RetryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.util.Set;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RecipientResolver recipientResolver(
            UserContactInfoRepository userContactInfoRepository,
            Set<NotificationSenderPort> notificationSenderPorts) {
        return new RecipientResolver(userContactInfoRepository, notificationSenderPorts);
    }

    @Bean
    public SubmitNotificationUseCase submitNotificationUseCase(
            RecipientResolver recipientResolver,
            NotificationRepositoryPort notificationRepository,
            Clock clock) {
        return new SubmitNotificationUseCaseImpl(recipientResolver, notificationRepository, clock);
    }

    @Bean
    public GetNotificationUseCase getNotificationUseCase(NotificationRepositoryPort notificationRepository) {
        return new GetNotificationUseCaseImpl(notificationRepository);
    }

    @Bean
    public RetryPolicy retryPolicy(
            @Value("${notifications.dispatch.max-attempts}") int maxAttempts,
            @Value("${notifications.dispatch.initial-backoff}") Duration initialBackoff,
            @Value("${notifications.dispatch.max-backoff}") Duration maxBackoff) {
        return new RetryPolicy(maxAttempts, initialBackoff, maxBackoff);
    }

    @Bean
    public DispatchNotificationsUseCase dispatchNotificationsUseCase(
            RecipientResolver recipientResolver,
            NotificationRepositoryPort notificationRepository,
            RetryPolicy retryPolicy,
            @Value("${notifications.dispatch.batch-size}") int batchSize,
            Clock clock) {
        return new DispatchNotificationsUseCaseImpl(recipientResolver, notificationRepository, retryPolicy, batchSize, clock);
    }
}
