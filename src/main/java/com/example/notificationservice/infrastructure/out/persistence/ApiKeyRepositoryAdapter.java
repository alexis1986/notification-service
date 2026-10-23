package com.example.notificationservice.infrastructure.out.persistence;

import com.example.notificationservice.application.ports.out.ApiKeyRepositoryPort;
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
    @Cacheable(value = "apiKeys", key = "#apiKey")
    public Optional<String> findClientIdByKey(String apiKey) {
        return jpaApiKeyRepository.findByApiKey(apiKey)
                .map(ApiKeyEntity::getClientId);
    }
}
