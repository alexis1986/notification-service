package com.alexdevvv.notifications.infrastructure.out.email;

import com.alexdevvv.notifications.domain.model.NotificationContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderAdapterTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    void send_shouldBuildMessageWithConfiguredSender() {
        EmailNotificationSenderAdapter adapter = new EmailNotificationSenderAdapter(javaMailSender, "no-reply@test.com");

        adapter.send("user@test.com", new NotificationContent("Subject", "Body"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@test.com");
        assertThat(message.getTo()).containsExactly("user@test.com");
        assertThat(message.getSubject()).isEqualTo("Subject");
        assertThat(message.getText()).isEqualTo("Body");
    }
}
