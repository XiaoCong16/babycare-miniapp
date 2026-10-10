package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.ReminderConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataReminderConfigRepository extends JpaRepository<ReminderConfig, Long> {
    Optional<ReminderConfig> findByUserId(Long userId);
}
