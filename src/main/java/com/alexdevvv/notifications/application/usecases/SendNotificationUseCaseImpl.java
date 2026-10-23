package com.alexdevvv.notifications.application.usecases;

import com.alexdevvv.notifications.application.ports.in.SendNotificationUseCase;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SendNotificationUseCaseImpl implements SendNotificationUseCase {

    private final UserContactInfoRepository userContactInfoRepository;
    private final Map<NotificationType, NotificationSenderPort> senders;

    public SendNotificationUseCaseImpl(UserContactInfoRepository userContactInfoRepository, Set<NotificationSenderPort> notificationSenderPorts) {
        this.userContactInfoRepository = userContactInfoRepository;
        this.senders = notificationSenderPorts.stream()
                .collect(Collectors.toMap(NotificationSenderPort::getType, Function.identity()));
    }

    @Override
    public void sendNotification(Notification notification) {
        UserContactInfo userContactInfo = userContactInfoRepository.findByClientIdAndUserId(notification.clientId(), notification.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + notification.userId() + " for client: " + notification.clientId()));

        NotificationType notificationType = Optional.ofNullable(notification.forcedType())
                .orElse(userContactInfo.preferredChannel());

        String recipient = getRecipientForType(userContactInfo, notificationType);

        NotificationSenderPort sender = Optional.ofNullable(senders.get(notificationType))
                .orElseThrow(() -> new IllegalArgumentException("No sender found for notification type: " + notificationType));
        
        sender.send(recipient, new NotificationContent(notification.subject(), notification.body()));
    }

    private String getRecipientForType(UserContactInfo userContactInfo, NotificationType type) {
        return switch (type) {
            case EMAIL -> userContactInfo.email();
            case WHATSAPP -> userContactInfo.phoneNumber();
            case PUSH -> userContactInfo.pushToken();
        };
    }
}
