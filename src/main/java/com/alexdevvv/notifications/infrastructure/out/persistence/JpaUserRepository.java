package com.alexdevvv.notifications.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, UserEntity.Key> {
}
