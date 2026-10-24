package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.application.ports.out.ApiKeyRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApiKeyRepositoryAdapter implements ApiKeyRepositoryPort {

    private final JpaApiKeyRepository jpaApiKeyRepository;

    public ApiKeyRepositoryAdapter(JpaApiKeyRepository jpaApiKeyRepository) {
        this.jpaApiKeyRepository = jpaApiKeyRepository;
    }

    @Override
    @Cacheable(value = "apiKeys", key = "T(com.alexdevvv.notifications.infrastructure.out.persistence.ApiKeyHasher).sha256(#apiKey)")
    public Optional<String> findClientIdByKey(String apiKey) {
        return jpaApiKeyRepository.findByKeyHash(ApiKeyHasher.sha256(apiKey))
                .map(ApiKeyEntity::getClientId);
    }
}
