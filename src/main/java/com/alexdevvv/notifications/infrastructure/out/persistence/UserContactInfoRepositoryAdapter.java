package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserContactInfoRepositoryAdapter implements UserContactInfoRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserContactInfoRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<UserContactInfo> findByClientIdAndUserId(String clientId, String userId) {
        return jpaUserRepository.findById(new UserEntity.Key(clientId, userId)).map(UserEntity::toDomain);
    }
}
