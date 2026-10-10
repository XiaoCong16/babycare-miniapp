package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.ReminderConfig;
import com.example.babycare.domain.repository.ReminderConfigRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaReminderConfigRepository implements ReminderConfigRepository {

    private final SpringDataReminderConfigRepository delegate;

    public JpaReminderConfigRepository(SpringDataReminderConfigRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ReminderConfig> findByUserId(Long userId) {
        return delegate.findByUserId(userId);
    }

    @Override
    public ReminderConfig save(ReminderConfig config) {
        return delegate.save(config);
    }
}
