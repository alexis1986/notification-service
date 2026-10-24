package com.alexdevvv.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * En prod las API Keys de demo no existen: el seed de db/demo no se aplica.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ProdProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demoApiKeyIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-API-Key", "key-for-client-a")
                        .contentType("application/json")
                        .content("{\"userId\":\"user1\", \"subject\":\"Hi\", \"body\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readinessIsUpWithSchemaOnly() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }
}
