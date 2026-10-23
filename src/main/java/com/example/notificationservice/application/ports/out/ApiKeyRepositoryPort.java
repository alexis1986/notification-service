package com.example.notificationservice.application.ports.out;

import java.util.Optional;

public interface ApiKeyRepositoryPort {
    Optional<String> findClientIdByKey(String apiKey);
}
