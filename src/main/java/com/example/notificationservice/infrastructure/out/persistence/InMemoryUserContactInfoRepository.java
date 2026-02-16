package com.example.notificationservice.infrastructure.out.persistence;

import com.example.notificationservice.application.ports.out.UserContactInfoRepository;
import com.example.notificationservice.domain.model.NotificationType;
import com.example.notificationservice.domain.model.UserContactInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserContactInfoRepository implements UserContactInfoRepository {

    private final Map<String, UserContactInfo> userContactInfoMap = new ConcurrentHashMap<>();

    public InMemoryUserContactInfoRepository() {
        // Pre-populate with some mock data
        userContactInfoMap.put("user1", new UserContactInfo(
                "user1",
                "user1@example.com",
                "+1111111111",
                "push-token-user1",
                NotificationType.EMAIL
        ));
        userContactInfoMap.put("user2", new UserContactInfo(
                "user2",
                "user2@example.com",
                "+2222222222",
                "push-token-user2",
                NotificationType.WHATSAPP
        ));
        userContactInfoMap.put("user3", new UserContactInfo(
                "user3",
                "user3@example.com",
                "+3333333333",
                "push-token-user3",
                NotificationType.PUSH
        ));
    }

    @Override
    public Optional<UserContactInfo> findByUserId(String userId) {
        return Optional.ofNullable(userContactInfoMap.get(userId));
    }
}
