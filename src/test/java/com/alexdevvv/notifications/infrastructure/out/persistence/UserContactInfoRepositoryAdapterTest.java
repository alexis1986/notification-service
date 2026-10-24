package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.TestcontainersConfiguration;
import com.alexdevvv.notifications.application.ports.out.UserContactInfoRepository;
import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lee los usuarios de demo cargados por Flyway (db/demo).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserContactInfoRepositoryAdapterTest {

    @Autowired
    private UserContactInfoRepository userContactInfoRepository;

    @Test
    void findsUserWithAllItsAddresses() {
        UserContactInfo user = userContactInfoRepository.findByClientIdAndUserId("client-a", "user1").orElseThrow();

        assertThat(user.userId()).isEqualTo("user1");
        assertThat(user.preferredChannel()).isEqualTo(NotificationType.EMAIL);
        assertThat(user.addressFor(NotificationType.EMAIL)).contains("delivered+user1-client-a@resend.dev");
        assertThat(user.addressFor(NotificationType.WHATSAPP)).contains("+1111111111");
        assertThat(user.addressFor(NotificationType.PUSH)).contains("push-a-1");
    }

    @Test
    void sameUserIdIsIndependentPerClient() {
        UserContactInfo user = userContactInfoRepository.findByClientIdAndUserId("client-b", "user1").orElseThrow();

        assertThat(user.preferredChannel()).isEqualTo(NotificationType.PUSH);
        assertThat(user.addressFor(NotificationType.EMAIL)).contains("delivered+user1-client-b@resend.dev");
    }

    @Test
    void unknownUserIsEmpty() {
        assertThat(userContactInfoRepository.findByClientIdAndUserId("client-b", "user2")).isEmpty();
    }
}
