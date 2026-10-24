package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ErrorHandlingTest {

    private static final String ENDPOINT = "/api/v1/notifications";
    private static final String VALID_KEY = "key-for-client-a";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenUserDoesNotExist_thenReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"ghost\", \"body\":\"Test\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void whenBodyIsInvalid_thenReturnsBadRequestProblem() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user1\", \"body\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void whenApiKeyIsMissing_thenReturnsUnauthorizedProblem() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user1\", \"body\":\"Test\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void whenQueryingStatusWithoutApiKey_thenReturnsUnauthorizedProblem() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenNotificationDoesNotExist_thenReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/" + UUID.randomUUID()).header("X-API-Key", VALID_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Notification not found"));
    }
}
