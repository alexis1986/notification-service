package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class OpenApiDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentDescribesEndpointsAndApiKeyAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/notifications'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/notifications/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/notifications'].post.parameters[?(@.name == 'Idempotency-Key')]").exists())
                .andExpect(jsonPath("$.components.securitySchemes.ApiKey.name").value("X-API-Key"));
    }
}
