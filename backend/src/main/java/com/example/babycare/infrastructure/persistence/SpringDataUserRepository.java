package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
}
