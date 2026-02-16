package com.example.notificationservice.application.usecases;

import com.example.notificationservice.application.ports.in.SendNotificationUseCase;
import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.application.ports.out.UserContactInfoRepository;
import com.example.notificationservice.domain.model.Notification;
import com.example.notificationservice.domain.model.NotificationContent;
import com.example.notificationservice.domain.model.NotificationType;
import com.example.notificationservice.domain.model.UserContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseImplTest {

    @Mock
    private UserContactInfoRepository userContactInfoRepository;
    @Mock
    private NotificationSenderPort emailSender;
    @Mock
    private NotificationSenderPort whatsappSender;
    @Mock
    private NotificationSenderPort pushSender;

    private SendNotificationUseCase sendNotificationUseCase;

    @BeforeEach
    void setUp() {
        when(emailSender.getType()).thenReturn(NotificationType.EMAIL);
        when(whatsappSender.getType()).thenReturn(NotificationType.WHATSAPP);
        when(pushSender.getType()).thenReturn(NotificationType.PUSH);

        Set<NotificationSenderPort> senders = new HashSet<>();
        senders.add(emailSender);
        senders.add(whatsappSender);
        senders.add(pushSender);

        sendNotificationUseCase = new SendNotificationUseCaseImpl(userContactInfoRepository, senders);
    }

    @Test
    void sendNotification_shouldUseUserPreferredChannel() {
        // Given
        String userId = "user1";
        UserContactInfo contactInfo = new UserContactInfo(userId, "user1@example.com", null, null, NotificationType.EMAIL);
        when(userContactInfoRepository.findByUserId(userId)).thenReturn(Optional.of(contactInfo));

        Notification notification = new Notification(userId, "Test Subject", "Test Body", null);
        NotificationContent expectedContent = new NotificationContent("Test Subject", "Test Body");

        // When
        sendNotificationUseCase.sendNotification(notification);

        // Then
        verify(userContactInfoRepository).findByUserId(userId);
        verify(emailSender).send("user1@example.com", expectedContent);
        verify(whatsappSender, never()).send(any(), any());
        verify(pushSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldUseForcedChannel() {
        // Given
        String userId = "user2";
        UserContactInfo contactInfo = new UserContactInfo(userId, "user2@example.com", null, "push-token", NotificationType.EMAIL);
        when(userContactInfoRepository.findByUserId(userId)).thenReturn(Optional.of(contactInfo));

        Notification notification = new Notification(userId, "Test Subject", "Test Body", NotificationType.PUSH);
        NotificationContent expectedContent = new NotificationContent("Test Subject", "Test Body");

        // When
        sendNotificationUseCase.sendNotification(notification);

        // Then
        verify(userContactInfoRepository).findByUserId(userId);
        verify(pushSender).send("push-token", expectedContent);
        verify(emailSender, never()).send(any(), any());
        verify(whatsappSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldThrowException_whenUserNotFound() {
        // Given
        String userId = "unknownUser";
        when(userContactInfoRepository.findByUserId(userId)).thenReturn(Optional.empty());
        Notification notification = new Notification(userId, "Subject", "Body", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sendNotificationUseCase.sendNotification(notification));
    }

    @Test
    void sendNotification_shouldThrowException_whenSenderNotFound() {
        // Given
        String userId = "user1";
        // User prefers WHATSAPP, but we haven't configured a sender for it in this specific test's setup
        UserContactInfo contactInfo = new UserContactInfo(userId, null, "+12345", null, NotificationType.WHATSAPP);
        when(userContactInfoRepository.findByUserId(userId)).thenReturn(Optional.of(contactInfo));

        // Let's remove the whatsapp sender for this test
        Set<NotificationSenderPort> senders = new HashSet<>();
        senders.add(emailSender);
        senders.add(pushSender);
        sendNotificationUseCase = new SendNotificationUseCaseImpl(userContactInfoRepository, senders);

        Notification notification = new Notification(userId, "Subject", "Body", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sendNotificationUseCase.sendNotification(notification));
    }
}

