package com.alexdevvv.notifications.infrastructure.out.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 sin salt alcanza para API Keys: son tokens aleatorios de alta entropía,
 * no contraseñas elegidas por humanos, y el hash determinístico permite buscarlas por índice.
 */
public final class ApiKeyHasher {

    private ApiKeyHasher() {
    }

    public static String sha256(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
