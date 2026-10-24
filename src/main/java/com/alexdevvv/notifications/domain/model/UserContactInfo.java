package com.alexdevvv.notifications.domain.model;

import java.util.Map;
import java.util.Optional;

/**
 * Las direcciones se indexan por canal para que agregar un NotificationType nuevo
 * no requiera cambiar este modelo ni el caso de uso (OCP).
 */
public record UserContactInfo(
        String userId,
        Map<NotificationType, String> addresses,
        NotificationType preferredChannel
) {

    public UserContactInfo {
        addresses = Map.copyOf(addresses);
    }

    public Optional<String> addressFor(NotificationType type) {
        return Optional.ofNullable(addresses.get(type))
                .filter(address -> !address.isBlank());
    }
}
