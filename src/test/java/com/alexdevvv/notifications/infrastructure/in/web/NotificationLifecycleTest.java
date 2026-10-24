package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import com.alexdevvv.notifications.infrastructure.in.scheduler.NotificationDispatchScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre el ciclo completo contra PostgreSQL: aceptación, despacho, reintentos y consulta de estado.
 * Con la configuración de test el scheduler no corre solo: cada test dispara el despacho.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class NotificationLifecycleTest {

    private static final String ENDPOINT = "/api/v1/notifications";
    private static final String CLIENT_A_KEY = "key-for-client-a";
    private static final String CLIENT_B_KEY = "super-secret-key-for-b";
    // user1 de client-a prefiere EMAIL
    private static final String EMAIL_NOTIFICATION = "{\"userId\":\"user1\", \"subject\":\"Hi\", \"body\":\"Test\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationDispatchScheduler dispatchScheduler;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void acceptedNotification_isDeliveredByTheDispatcher() throws Exception {
        String location = submit(EMAIL_NOTIFICATION);

        mockMvc.perform(get(location).header("X-API-Key", CLIENT_A_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        dispatchScheduler.dispatch();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        mockMvc.perform(get(location).header("X-API-Key", CLIENT_A_KEY))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.attempts").value(1));
    }

    @Test
    void providerFailures_areRetriedUntilTheNotificationFails() throws Exception {
        doThrow(new MailSendException("SMTP down")).when(javaMailSender).send(any(SimpleMailMessage.class));
        String location = submit(EMAIL_NOTIFICATION);

        dispatchScheduler.dispatch();
        mockMvc.perform(get(location).header("X-API-Key", CLIENT_A_KEY))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.attempts").value(1))
                .andExpect(jsonPath("$.lastError").isNotEmpty());

        // max-attempts=3 y backoff 0 en la configuración de test
        dispatchScheduler.dispatch();
        dispatchScheduler.dispatch();
        mockMvc.perform(get(location).header("X-API-Key", CLIENT_A_KEY))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.attempts").value(3));
    }

    @Test
    void notifications_areNotVisibleToOtherClients() throws Exception {
        String location = submit(EMAIL_NOTIFICATION);

        mockMvc.perform(get(location).header("X-API-Key", CLIENT_B_KEY))
                .andExpect(status().isNotFound());
    }

    private String submit(String body) throws Exception {
        return mockMvc.perform(post(ENDPOINT)
                        .header("X-API-Key", CLIENT_A_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.LOCATION);
    }
}
