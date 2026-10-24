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
                .orElseThrow(() -> new UserNotFoundException(notification.clientId(), notification.userId()));

        NotificationType notificationType = Optional.ofNullable(notification.forcedType())
                .orElse(userContactInfo.preferredChannel());

        NotificationSenderPort sender = Optional.ofNullable(senders.get(notificationType))
                .orElseThrow(() -> new UnsupportedChannelException(notificationType));

        String recipient = userContactInfo.addressFor(notificationType)
                .orElseThrow(() -> new MissingContactInfoException(userContactInfo.userId(), notificationType));

        sender.send(recipient, new NotificationContent(notification.subject(), notification.body()));
    }
}
