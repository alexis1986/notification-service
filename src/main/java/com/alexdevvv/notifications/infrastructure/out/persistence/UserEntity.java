package com.alexdevvv.notifications.infrastructure.out.persistence;

import com.alexdevvv.notifications.domain.model.NotificationType;
import com.alexdevvv.notifications.domain.model.UserContactInfo;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "users")
public class UserEntity {

    @EmbeddedId
    private Key id;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", nullable = false, length = 20)
    private NotificationType preferredChannel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_addresses", joinColumns = {
            @JoinColumn(name = "client_id", referencedColumnName = "client_id"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "channel", length = 20)
    @Column(name = "address", nullable = false)
    private Map<NotificationType, String> addresses = new HashMap<>();

    protected UserEntity() {
    }

    UserContactInfo toDomain() {
        return new UserContactInfo(id.userId, addresses, preferredChannel);
    }

    @Embeddable
    public static class Key implements Serializable {

        @Column(name = "client_id", nullable = false)
        private String clientId;

        @Column(name = "user_id", nullable = false)
        private String userId;

        protected Key() {
        }

        Key(String clientId, String userId) {
            this.clientId = clientId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key other && clientId.equals(other.clientId) && userId.equals(other.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, userId);
        }
    }
}
