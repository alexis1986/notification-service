package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.SendNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.exceptions.MissingContactInfoException;
import com.alexdevvv.notifications.domain.exceptions.UnsupportedChannelException;
import com.alexdevvv.notifications.domain.exceptions.UserNotFoundException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    void sendNotification_shouldUseUserPreferredChannel_forSpecificClient() {
        // Given
        String clientId = "client-a";
        String userId = "user1";
        UserContactInfo contactInfo = new UserContactInfo(userId, "user1@client-a.com", null, null, NotificationType.EMAIL);
        when(userContactInfoRepository.findByClientIdAndUserId(eq(clientId), eq(userId))).thenReturn(Optional.of(contactInfo));

        Notification notification = new Notification(clientId, userId, "Test Subject", "Test Body", null);
        NotificationContent expectedContent = new NotificationContent("Test Subject", "Test Body");

        // When
        sendNotificationUseCase.sendNotification(notification);

        // Then
        verify(userContactInfoRepository).findByClientIdAndUserId(eq(clientId), eq(userId));
        verify(emailSender).send("user1@client-a.com", expectedContent);
        verify(whatsappSender, never()).send(any(), any());
        verify(pushSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldUseForcedChannel_forSpecificClient() {
        // Given
        String clientId = "client-b";
        String userId = "user1";
        UserContactInfo contactInfo = new UserContactInfo(userId, "user1@client-b.com", null, "push-b-1", NotificationType.EMAIL); // Preferred is EMAIL, but forced to PUSH
        when(userContactInfoRepository.findByClientIdAndUserId(eq(clientId), eq(userId))).thenReturn(Optional.of(contactInfo));

        Notification notification = new Notification(clientId, userId, "Test Subject", "Test Body", NotificationType.PUSH);
        NotificationContent expectedContent = new NotificationContent("Test Subject", "Test Body");

        // When
        sendNotificationUseCase.sendNotification(notification);

        // Then
        verify(userContactInfoRepository).findByClientIdAndUserId(eq(clientId), eq(userId));
        verify(pushSender).send("push-b-1", expectedContent);
        verify(emailSender, never()).send(any(), any());
        verify(whatsappSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldThrowException_whenUserNotFoundForClient() {
        // Given
        String clientId = "client-a";
        String userId = "unknownUser";
        when(userContactInfoRepository.findByClientIdAndUserId(eq(clientId), eq(userId))).thenReturn(Optional.empty());
        Notification notification = new Notification(clientId, userId, "Subject", "Body", null);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> sendNotificationUseCase.sendNotification(notification));
        verify(userContactInfoRepository).findByClientIdAndUserId(eq(clientId), eq(userId));
        verify(emailSender, never()).send(any(), any());
        verify(whatsappSender, never()).send(any(), any());
        verify(pushSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldThrowException_whenUserHasNoContactForChannel() {
        // Given
        String clientId = "client-a";
        String userId = "user1";
        UserContactInfo contactInfo = new UserContactInfo(userId, "user1@client-a.com", null, null, NotificationType.EMAIL);
        when(userContactInfoRepository.findByClientIdAndUserId(eq(clientId), eq(userId))).thenReturn(Optional.of(contactInfo));

        Notification notification = new Notification(clientId, userId, "Subject", "Body", NotificationType.WHATSAPP);

        // When & Then
        assertThrows(MissingContactInfoException.class, () -> sendNotificationUseCase.sendNotification(notification));
        verify(whatsappSender, never()).send(any(), any());
    }

    @Test
    void sendNotification_shouldThrowException_whenSenderNotFoundForType() {
        // Given
        String clientId = "client-a";
        String userId = "user2";
        // user2 prefers WHATSAPP, but we'll remove whatsappSender from the use case for this test
        UserContactInfo contactInfo = new UserContactInfo(userId, null, "+12345", null, NotificationType.WHATSAPP);
        when(userContactInfoRepository.findByClientIdAndUserId(eq(clientId), eq(userId))).thenReturn(Optional.of(contactInfo));

        // Create use case without whatsappSender
        Set<NotificationSenderPort> senders = new HashSet<>();
        senders.add(emailSender);
        senders.add(pushSender);
        sendNotificationUseCase = new SendNotificationUseCaseImpl(userContactInfoRepository, senders);

        Notification notification = new Notification(clientId, userId, "Subject", "Body", null);

        // When & Then
        assertThrows(UnsupportedChannelException.class, () -> sendNotificationUseCase.sendNotification(notification));
        verify(userContactInfoRepository).findByClientIdAndUserId(eq(clientId), eq(userId));
        verify(emailSender, never()).send(any(), any());
        verify(whatsappSender, never()).send(any(), any());
        verify(pushSender, never()).send(any(), any());
    }
}
