package com.alexdevvv.notifications.infrastructure.out.email;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import com.alexdevvv.notifications.application.ports.out.NotificationSenderPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Con provider=resend solo queda un sender de EMAIL: dos romperían el mapa de estrategias.
 */
@SpringBootTest(properties = {"notifications.mail.provider=resend", "notifications.mail.resend.api-key=re_test"})
@Import(TestcontainersConfiguration.class)
class MailProviderSelectionTest {

    @Autowired
    private List<NotificationSenderPort> senders;

    @Test
    void resendReplacesSmtpSender() {
        assertThat(senders)
                .hasAtLeastOneElementOfType(ResendEmailSenderAdapter.class)
                .noneMatch(EmailNotificationSenderAdapter.class::isInstance);
    }
}
