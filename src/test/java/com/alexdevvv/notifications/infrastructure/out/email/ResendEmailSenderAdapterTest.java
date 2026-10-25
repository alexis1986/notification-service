package com.alexdevvv.notifications.infrastructure.out.email;

import com.alexdevvv.notifications.domain.exceptions.DeliveryRejectedException;
import com.alexdevvv.notifications.domain.exceptions.NotificationDeliveryException;
import com.alexdevvv.notifications.domain.exceptions.UndeliverableNotificationException;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResendEmailSenderAdapterTest {

    private MockRestServiceServer server;
    private ResendEmailSenderAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new ResendEmailSenderAdapter(builder, "https://api.resend.test", "re_test", "no-reply@test.com");
    }

    @Test
    void send_postsEmailWithBearerAuth() {
        server.expect(requestTo("https://api.resend.test/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_test"))
                .andExpect(jsonPath("$.from").value("no-reply@test.com"))
                .andExpect(jsonPath("$.to[0]").value("user@test.com"))
                .andExpect(jsonPath("$.subject").value("Subject"))
                .andExpect(jsonPath("$.text").value("Body"))
                .andRespond(withSuccess("{\"id\":\"email-123\"}", MediaType.APPLICATION_JSON));

        adapter.send("user@test.com", new NotificationContent("Subject", "Body"));

        server.verify();
    }

    @Test
    void send_wrapsProviderErrorsAsDeliveryFailure() {
        server.expect(requestTo("https://api.resend.test/emails"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.send("user@test.com", new NotificationContent("Subject", "Body")))
                .isInstanceOf(NotificationDeliveryException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 422})
    void send_contentRejectionIsPermanent(int status) {
        server.expect(requestTo("https://api.resend.test/emails"))
                .andRespond(withStatus(HttpStatus.valueOf(status))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":" + status + ",\"name\":\"validation_error\",\"message\":\"Invalid `to` field.\"}"));

        assertThatThrownBy(() -> adapter.send("not-an-email", new NotificationContent("Subject", "Body")))
                .isInstanceOf(DeliveryRejectedException.class)
                .isInstanceOf(UndeliverableNotificationException.class)
                .hasMessageContaining(String.valueOf(status))
                .hasMessageContaining("Invalid `to` field.");
    }

    // Credenciales o límite de tasa: se arreglan sin tocar la notificación, así que se reintentan
    @ParameterizedTest
    @ValueSource(ints = {401, 403, 429})
    void send_configurationAndRateLimitErrorsAreRetried(int status) {
        server.expect(requestTo("https://api.resend.test/emails"))
                .andRespond(withStatus(HttpStatus.valueOf(status)));

        assertThatThrownBy(() -> adapter.send("user@test.com", new NotificationContent("Subject", "Body")))
                .isInstanceOf(NotificationDeliveryException.class);
    }
}
