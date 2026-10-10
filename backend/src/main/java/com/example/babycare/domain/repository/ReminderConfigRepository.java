package com.example.babycare.domain.repository;

import com.example.babycare.domain.model.ReminderConfig;

import java.util.Optional;

public interface ReminderConfigRepository {
    Optional<ReminderConfig> findByUserId(Long userId);
    ReminderConfig save(ReminderConfig config);
}
