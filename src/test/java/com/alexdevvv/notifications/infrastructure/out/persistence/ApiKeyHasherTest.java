package com.alexdevvv.notifications.infrastructure.out.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyHasherTest {

    @Test
    void sha256_shouldReturnLowercaseHexDigest() {
        assertThat(ApiKeyHasher.sha256("key-for-client-a"))
                .isEqualTo("94562fa3e0c6846a1f7bfe7b351e89eda94f3b5bd384d442e31d8de68ae4a6ff");
    }
}
