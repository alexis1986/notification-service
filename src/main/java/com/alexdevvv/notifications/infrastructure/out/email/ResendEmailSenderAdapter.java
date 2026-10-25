package com.alexdevvv.notifications.infrastructure.out.email;

import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import com.alexdevvv.notifications.domain.exceptions.DeliveryRejectedException;
import com.alexdevvv.notifications.domain.exceptions.NotificationDeliveryException;
import com.alexdevvv.notifications.domain.model.NotificationContent;
import com.alexdevvv.notifications.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;

/**
 * Envío por la API HTTP de Resend: en DigitalOcean el SMTP saliente está bloqueado por defecto.
 */
@Component
@ConditionalOnProperty(name = "notifications.mail.provider", havingValue = "resend")
public class ResendEmailSenderAdapter implements NotificationSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(ResendEmailSenderAdapter.class);

    // Solo errores del contenido: 401/403 (credenciales) y 429 (cuota) se arreglan sin tocar la notificación
    private static final Set<Integer> PERMANENT_STATUSES = Set.of(400, 422);

    private final RestClient restClient;
    private final String from;

    public ResendEmailSenderAdapter(RestClient.Builder restClientBuilder,
                                    @Value("${notifications.mail.resend.base-url}") String baseUrl,
                                    @Value("${notifications.mail.resend.api-key}") String apiKey,
                                    @Value("${notifications.mail.from}") String from) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.from = from;
    }

    @Override
    public void send(String recipient, NotificationContent content) {
        ResendEmail email = new ResendEmail(from, List.of(recipient), content.subject(), content.body());

        ResendResponse response;
        try {
            response = restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(email)
                    .retrieve()
                    .body(ResendResponse.class);
        } catch (HttpClientErrorException e) {
            if (PERMANENT_STATUSES.contains(e.getStatusCode().value())) {
                throw new DeliveryRejectedException(NotificationType.EMAIL, e.getStatusCode().value(), reasonOf(e));
            }
            throw new NotificationDeliveryException(NotificationType.EMAIL, e);
        } catch (RestClientException e) {
            throw new NotificationDeliveryException(NotificationType.EMAIL, e);
        }
        logger.info("Email sent to: {} with subject: {} (resend id: {})",
                recipient, content.subject(), response != null ? response.id() : null);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    record ResendEmail(String from, List<String> to, String subject, String text) {
    }

    private static String reasonOf(HttpClientErrorException e) {
        try {
            ResendError error = e.getResponseBodyAs(ResendError.class);
            if (error != null && error.message() != null) {
                return error.message();
            }
        } catch (RuntimeException ignored) {
            // Cuerpo vacío o no JSON: alcanza con el status
        }
        return e.getStatusText();
    }

    record ResendResponse(String id) {
    }

    record ResendError(String name, String message) {
    }
}
