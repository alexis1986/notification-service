package com.alexdevvv.notifications.domain.model;

import java.time.Duration;

/**
 * Backoff exponencial: initialBackoff * 2^(intento-1), acotado por maxBackoff.
 */
public record RetryPolicy(int maxAttempts, Duration initialBackoff, Duration maxBackoff) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
    }

    public boolean isExhausted(int attempts) {
        return attempts >= maxAttempts;
    }

    public Duration backoffAfter(int attempts) {
        // El tope del exponente evita overflow con valores altos de maxAttempts
        Duration backoff = initialBackoff.multipliedBy(1L << Math.min(attempts - 1, 30));
        return backoff.compareTo(maxBackoff) > 0 ? maxBackoff : backoff;
    }
}
