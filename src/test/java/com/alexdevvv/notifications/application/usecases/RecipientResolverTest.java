package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.exceptions.MissingContactInfoException;
import com.alexdevvv.notifications.domain.exceptions.UnsupportedChannelException;
import com.alexdevvv.notifications.domain.exceptions.UserNotFoundException;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientResolverTest {

    @Mock
    private UserContactInfoRepository userContactInfoRepository;
    @Mock
    private NotificationSenderPort emailSender;
    @Mock
    private NotificationSenderPort pushSender;

    private RecipientResolver recipientResolver;

    @BeforeEach
    void setUp() {
        when(emailSender.getType()).thenReturn(NotificationType.EMAIL);
        when(pushSender.getType()).thenReturn(NotificationType.PUSH);
        recipientResolver = new RecipientResolver(userContactInfoRepository, Set.of(emailSender, pushSender));
    }

    @Test
    void resolve_shouldUseUserPreferredChannel() {
        givenUser("client-a", new UserContactInfo("user1", Map.of(NotificationType.EMAIL, "user1@client-a.com"), NotificationType.EMAIL));

        RecipientResolver.Delivery delivery = recipientResolver.resolve(new Notification("client-a", "user1", "Subject", "Body", null));

        assertThat(delivery.sender()).isSameAs(emailSender);
        assertThat(delivery.recipient()).isEqualTo("user1@client-a.com");
    }

    @Test
    void resolve_shouldUseForcedChannel() {
        givenUser("client-b", new UserContactInfo("user1",
                Map.of(NotificationType.EMAIL, "user1@client-b.com", NotificationType.PUSH, "push-b-1"),
                NotificationType.EMAIL));

        RecipientResolver.Delivery delivery = recipientResolver.resolve(new Notification("client-b", "user1", "Subject", "Body", NotificationType.PUSH));

        assertThat(delivery.sender()).isSameAs(pushSender);
        assertThat(delivery.recipient()).isEqualTo("push-b-1");
    }

    @Test
    void resolve_shouldFail_whenUserNotFoundForClient() {
        when(userContactInfoRepository.findByClientIdAndUserId("client-a", "unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> recipientResolver.resolve(new Notification("client-a", "unknown", "Subject", "Body", null)));
    }

    @Test
    void resolve_shouldFail_whenUserHasNoAddressForChannel() {
        givenUser("client-a", new UserContactInfo("user1", Map.of(NotificationType.EMAIL, "user1@client-a.com"), NotificationType.EMAIL));

        assertThrows(MissingContactInfoException.class,
                () -> recipientResolver.resolve(new Notification("client-a", "user1", "Subject", "Body", NotificationType.PUSH)));
    }

    @Test
    void resolve_shouldFail_whenNoSenderForChannel() {
        givenUser("client-a", new UserContactInfo("user2", Map.of(NotificationType.WHATSAPP, "+12345"), NotificationType.WHATSAPP));

        assertThrows(UnsupportedChannelException.class,
                () -> recipientResolver.resolve(new Notification("client-a", "user2", "Subject", "Body", null)));
    }

    private void givenUser(String clientId, UserContactInfo contactInfo) {
        when(userContactInfoRepository.findByClientIdAndUserId(clientId, contactInfo.userId())).thenReturn(Optional.of(contactInfo));
    }
}
