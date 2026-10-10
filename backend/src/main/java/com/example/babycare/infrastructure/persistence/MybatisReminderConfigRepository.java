package com.example.babycare.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.babycare.domain.model.ReminderConfig;
import com.example.babycare.domain.repository.ReminderConfigRepository;
import com.example.babycare.infrastructure.persistence.mapper.ReminderConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisReminderConfigRepository implements ReminderConfigRepository {

    private final ReminderConfigMapper mapper;

    public MybatisReminderConfigRepository(ReminderConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ReminderConfig> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ReminderConfig>()
                .eq(ReminderConfig::getUserId, userId)));
    }

    @Override
    public ReminderConfig save(ReminderConfig config) {
        if (config.getId() == null) {
            mapper.insert(config);
        } else {
            mapper.updateById(config);
        }
        return config;
    }
}
