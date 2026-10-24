package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class IdempotencyKeyTest {

    private static final String ENDPOINT = "/api/v1/notifications";
    private static final String CLIENT_A_KEY = "key-for-client-a";
    private static final String CLIENT_B_KEY = "super-secret-key-for-b";
    private static final String REQUEST = "{\"userId\":\"user1\", \"subject\":\"Hi\", \"body\":\"Test\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void replayingTheSameRequest_returnsTheOriginalNotification() throws Exception {
        String key = UUID.randomUUID().toString();

        String first = submit(CLIENT_A_KEY, key, REQUEST).andExpect(status().isAccepted()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        String replay = submit(CLIENT_A_KEY, key, REQUEST).andExpect(status().isAccepted()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(replay).isEqualTo(first);
    }

    @Test
    void reusingTheKeyWithAnotherRequest_isRejected() throws Exception {
        String key = UUID.randomUUID().toString();
        submit(CLIENT_A_KEY, key, REQUEST).andExpect(status().isAccepted());

        submit(CLIENT_A_KEY, key, "{\"userId\":\"user1\", \"subject\":\"Hi\", \"body\":\"Another body\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Idempotency-Key reused"));
    }

    @Test
    void keysAreScopedPerClient() throws Exception {
        String key = UUID.randomUUID().toString();

        String clientA = submit(CLIENT_A_KEY, key, REQUEST).andExpect(status().isAccepted()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        String clientB = submit(CLIENT_B_KEY, key, REQUEST).andExpect(status().isAccepted()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(clientB).isNotEqualTo(clientA);
    }

    @Test
    void tooLongKey_isRejected() throws Exception {
        submit(CLIENT_A_KEY, "k".repeat(256), REQUEST).andExpect(status().isBadRequest());
    }

    private ResultActions submit(String apiKey, String idempotencyKey, String body) throws Exception {
        return mockMvc.perform(post(ENDPOINT)
                .header("X-API-Key", apiKey)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
