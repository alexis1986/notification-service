package com.alexdevvv.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserContactInfoTest {

    @Test
    void addressFor_shouldReturnAddressForConfiguredChannel() {
        UserContactInfo contactInfo = new UserContactInfo("user1", Map.of(NotificationType.EMAIL, "user1@test.com"), NotificationType.EMAIL);

        assertThat(contactInfo.addressFor(NotificationType.EMAIL)).contains("user1@test.com");
    }

    @Test
    void addressFor_shouldBeEmpty_whenChannelIsNotConfigured() {
        UserContactInfo contactInfo = new UserContactInfo("user1", Map.of(NotificationType.EMAIL, "user1@test.com"), NotificationType.EMAIL);

        assertThat(contactInfo.addressFor(NotificationType.PUSH)).isEmpty();
    }

    @Test
    void addressFor_shouldBeEmpty_whenAddressIsBlank() {
        UserContactInfo contactInfo = new UserContactInfo("user1", Map.of(NotificationType.WHATSAPP, " "), NotificationType.WHATSAPP);

        assertThat(contactInfo.addressFor(NotificationType.WHATSAPP)).isEmpty();
    }

    @Test
    void addresses_shouldBeImmutableCopy() {
        Map<NotificationType, String> addresses = new HashMap<>(Map.of(NotificationType.EMAIL, "user1@test.com"));
        UserContactInfo contactInfo = new UserContactInfo("user1", addresses, NotificationType.EMAIL);

        addresses.put(NotificationType.PUSH, "push-token");

        assertThat(contactInfo.addressFor(NotificationType.PUSH)).isEmpty();
    }
}
