package com.example.babycare.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reminder_config")
public class ReminderConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer feedIntervalMinutes = 180;

    private Integer diaperIntervalMinutes = 240;

    private Boolean reminderEnabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ReminderConfig() {
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
