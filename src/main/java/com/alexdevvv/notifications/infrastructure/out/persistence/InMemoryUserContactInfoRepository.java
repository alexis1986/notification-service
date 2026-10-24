package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserContactInfoRepository implements UserContactInfoRepository {

    private final Map<String, UserContactInfo> userContactInfoMap = new ConcurrentHashMap<>();

    public InMemoryUserContactInfoRepository() {
        // Pre-populate with some mock data
        // User 1 has different preferences per client
        userContactInfoMap.put("client-a:user1", user("user1", "user1@client-a.com", "+1111111111", "push-a-1", NotificationType.EMAIL));
        userContactInfoMap.put("client-b:user1", user("user1", "user1@client-b.com", "+2222222222", "push-b-1", NotificationType.PUSH));

        // User 2 only exists for client-a
        userContactInfoMap.put("client-a:user2", user("user2", "user2@client-a.com", "+3333333333", "push-a-2", NotificationType.WHATSAPP));
    }

    private static UserContactInfo user(String userId, String email, String phone, String pushToken, NotificationType preferred) {
        return new UserContactInfo(userId, Map.of(
                NotificationType.EMAIL, email,
                NotificationType.WHATSAPP, phone,
                NotificationType.PUSH, pushToken
        ), preferred);
    }

    @Override
    public Optional<UserContactInfo> findByClientIdAndUserId(String clientId, String userId) {
        return Optional.ofNullable(userContactInfoMap.get(String.format("%s:%s", clientId, userId)));
    }
}
