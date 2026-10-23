package com.example.notificationservice.infrastructure.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    public ApiKeyEntity() {
    }

    public ApiKeyEntity(String clientId, String apiKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
    }

    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
