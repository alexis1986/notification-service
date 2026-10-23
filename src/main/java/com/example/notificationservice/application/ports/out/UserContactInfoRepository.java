package com.example.notificationservice.application.ports.out;

import com.example.notificationservice.domain.model.UserContactInfo;

import java.util.Optional;

public interface UserContactInfoRepository {
    Optional<UserContactInfo> findByClientIdAndUserId(String clientId, String userId);
}
