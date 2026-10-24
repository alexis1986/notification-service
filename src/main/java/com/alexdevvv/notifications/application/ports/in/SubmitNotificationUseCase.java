package com.alexdevvv.notifications.application.ports.in;

import com.alexdevvv.notifications.domain.model.Notification;
import com.alexdevvv.notifications.domain.model.TrackedNotification;

public interface SubmitNotificationUseCase {
    TrackedNotification submit(Notification notification);
}
