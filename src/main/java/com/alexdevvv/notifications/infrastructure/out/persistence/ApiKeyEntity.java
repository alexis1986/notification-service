package com.alexdevvv.notifications.infrastructure.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    public ApiKeyEntity() {
    }

    public ApiKeyEntity(String clientId, String keyHash) {
        this.clientId = clientId;
        this.keyHash = keyHash;
    }

    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
