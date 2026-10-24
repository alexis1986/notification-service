package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.application.ports.out.ApiKeyRepositoryPort;
import com.alexdevvv.notifications.infrastructure.out.persistence.ApiKeyEntity;
import com.alexdevvv.notifications.infrastructure.out.persistence.ApiKeyHasher;
import com.alexdevvv.notifications.infrastructure.out.persistence.JpaApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaApiKeyRepository jpaApiKeyRepository;

    @Autowired
    private ApiKeyRepositoryPort apiKeyRepositoryPort;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void whenValidApiKey_thenReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-API-Key", "key-for-client-a")
                        .contentType("application/json")
                        .content("{\"userId\":\"user2\", \"subject\":\"Hi\", \"body\":\"Test\"}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void whenInvalidApiKey_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-API-Key", "wrong-key")
                        .contentType("application/json")
                        .content("{\"userId\":\"user2\", \"subject\":\"Hi\", \"body\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiKeysShouldNotBeStoredInPlainText() {
        assertThat(jpaApiKeyRepository.findAll())
                .extracting(ApiKeyEntity::getKeyHash)
                .isNotEmpty()
                .doesNotContain("key-for-client-a", "super-secret-key-for-b")
                .allMatch(hash -> hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void whenApiKeyIsUsed_itShouldBeCached() {
        String apiKey = "key-for-cache-test";
        String keyHash = ApiKeyHasher.sha256(apiKey);
        String clientId = "client-cache-test";

        // Setup new key in DB
        jpaApiKeyRepository.save(new ApiKeyEntity(clientId, keyHash));

        // Ensure cache is empty for this key
        cacheManager.getCache("apiKeys").evict(keyHash);

        // First call - should go to DB
        Optional<String> result = apiKeyRepositoryPort.findClientIdByKey(apiKey);
        assertThat(result).isPresent().contains(clientId);

        // Check if it's in cache, keyed by hash so the raw key is never held in memory
        Object cachedValue = cacheManager.getCache("apiKeys").get(keyHash).get();
        assertThat(cachedValue).isEqualTo(clientId);

        // Change DB value
        ApiKeyEntity entity = jpaApiKeyRepository.findByKeyHash(keyHash).get();
        entity.setClientId("new-client-id");
        jpaApiKeyRepository.save(entity);

        // Second call - should return cached value (old value)
        Optional<String> cachedResult = apiKeyRepositoryPort.findClientIdByKey(apiKey);
        assertThat(cachedResult).contains(clientId);

        // Evict cache
        cacheManager.getCache("apiKeys").evict(keyHash);
        
        // Third call - should return new value from DB
        Optional<String> newResult = apiKeyRepositoryPort.findClientIdByKey(apiKey);
        assertThat(newResult).contains("new-client-id");
    }
}
