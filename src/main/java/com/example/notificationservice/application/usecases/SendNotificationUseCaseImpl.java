package com.example.notificationservice.application.usecases;

import com.example.notificationservice.application.ports.in.SendNotificationUseCase;
import com.example.notificationservice.application.ports.out.NotificationSenderPort;
import com.example.notificationservice.domain.model.NotificationRequest;
import com.example.notificationservice.domain.model.NotificationType;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SendNotificationUseCaseImpl implements SendNotificationUseCase {

    private final Map<NotificationType, NotificationSenderPort> senders;

    public SendNotificationUseCaseImpl(Set<NotificationSenderPort> notificationSenderPorts) {
        this.senders = notificationSenderPorts.stream()
                .collect(Collectors.toMap(NotificationSenderPort::getType, Function.identity()));
    }


    @Override
    public void sendNotification(NotificationRequest request) {
        NotificationSenderPort sender = senders.get(request.getType());
        if (sender == null) {
            throw new IllegalArgumentException("No sender found for notification type: " + request.getType());
        }
        sender.sendNotification(request);
    }
}
