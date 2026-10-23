package com.alexdevvv.notifications.application.ports.out;

import com.alexdevvv.notifications.domain.model.UserContactInfo;

import java.util.Optional;

public interface UserContactInfoRepository {
    Optional<UserContactInfo> findByClientIdAndUserId(String clientId, String userId);
}
