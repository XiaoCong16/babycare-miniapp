package com.example.babycare.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reminder_config")
public class ReminderConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "feed_interval_minutes")
    private Integer feedIntervalMinutes = 180;

    @Column(name = "diaper_interval_minutes")
    private Integer diaperIntervalMinutes = 240;

    @Column(name = "reminder_enabled")
    private Boolean reminderEnabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ReminderConfig() {
    }

    public static ReminderConfig defaultConfig(Long userId) {
        ReminderConfig config = new ReminderConfig();
        config.setUserId(userId);
        config.setFeedIntervalMinutes(180);
        config.setDiaperIntervalMinutes(240);
        config.setReminderEnabled(true);
        return config;
    }
}
