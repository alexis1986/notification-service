package com.example.notificationservice.application.usecases;

import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseImplTest {

    private SendNotificationUseCaseImpl sendNotificationUseCase;

    @Mock
    private NotificationSenderPort emailSenderPort;
    @Mock
    private NotificationSenderPort whatsappSenderPort;
    @Mock
    private NotificationSenderPort pushSenderPort;

    @BeforeEach
    void setUp() {
        when(emailSenderPort.getType()).thenReturn(NotificationType.EMAIL);
        when(whatsappSenderPort.getType()).thenReturn(NotificationType.WHATSAPP);
        when(pushSenderPort.getType()).thenReturn(NotificationType.PUSH);

        Set<NotificationSenderPort> senderPorts = new HashSet<>();
        senderPorts.add(emailSenderPort);
        senderPorts.add(whatsappSenderPort);
        senderPorts.add(pushSenderPort);

        sendNotificationUseCase = new SendNotificationUseCaseImpl(senderPorts);
    }

    @Test
    void sendEmailNotification_shouldCallEmailSender() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@example.com")
                .subject("Test Email")
                .body("This is a test email.")
                .type(NotificationType.EMAIL)
                .build();

        sendNotificationUseCase.sendNotification(request);

        verify(emailSenderPort, times(1)).sendNotification(request);
        verify(whatsappSenderPort, never()).sendNotification(any());
        verify(pushSenderPort, never()).sendNotification(any());
    }

    @Test
    void sendWhatsappNotification_shouldCallWhatsappSender() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("+1234567890")
                .body("This is a test WhatsApp message.")
                .type(NotificationType.WHATSAPP)
                .build();

        sendNotificationUseCase.sendNotification(request);

        verify(whatsappSenderPort, times(1)).sendNotification(request);
        verify(emailSenderPort, never()).sendNotification(any());
        verify(pushSenderPort, never()).sendNotification(any());
    }

    @Test
    void sendPushNotification_shouldCallPushSender() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("device_token_123")
                .subject("Test Push")
                .body("This is a test push notification.")
                .type(NotificationType.PUSH)
                .build();

        sendNotificationUseCase.sendNotification(request);

        verify(pushSenderPort, times(1)).sendNotification(request);
        verify(emailSenderPort, never()).sendNotification(any());
        verify(whatsappSenderPort, never()).sendNotification(any());
    }

    @Test
    void sendNotification_withUnknownType_shouldThrowException() {
        // This test relies on the constructor's behavior when it can't map a sender type
        // For this mock setup, it implicitly covers cases where a sender isn't found
        // or if the internal mapping logic fails to identify a mock based on its name.
        // A more explicit test would involve a custom type or an unmapped mock.

        // Given our current implementation, if a type is not recognized during construction,
        // it would throw an exception during construction. If a type is recognized
        // during construction but no sender exists in the map for a given request.getType(),
        // it would throw an IllegalArgumentException.

        NotificationRequest request = NotificationRequest.builder()
                .recipient("any")
                .body("any")
                .type(null) // Deliberately null to trigger failure
                .build();

        // The current constructor logic would throw if `type` was used to identify sender.
        // The current test scenario directly tests the `sendNotification` method.
        // If type is null, `senderMap.get(request.getType())` would return null.
        assertThrows(IllegalArgumentException.class, () -> sendNotificationUseCase.sendNotification(request));
    }
}
