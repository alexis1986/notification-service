package com.alexdevvv.notifications.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    Optional<ApiKeyEntity> findByApiKey(String apiKey);
}
