package com.example.babycare.application.service;

import com.example.babycare.domain.model.BabyRecord;
import com.example.babycare.domain.model.ReminderConfig;
import com.example.babycare.domain.model.User;
import com.example.babycare.domain.repository.BabyRecordRepository;
import com.example.babycare.domain.repository.ReminderConfigRepository;
import com.example.babycare.domain.repository.UserRepository;
import com.example.babycare.infrastructure.wechat.WeChatClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final UserRepository userRepository;
    private final ReminderConfigRepository reminderConfigRepository;
    private final BabyRecordRepository babyRecordRepository;
    private final WeChatClient weChatClient;

    public ReminderService(UserRepository userRepository,
                          ReminderConfigRepository reminderConfigRepository,
                          BabyRecordRepository babyRecordRepository,
                          WeChatClient weChatClient) {
        this.userRepository = userRepository;
        this.reminderConfigRepository = reminderConfigRepository;
        this.babyRecordRepository = babyRecordRepository;
        this.weChatClient = weChatClient;
    }

    public void checkAndSendReminders() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            ReminderConfig config = reminderConfigRepository.findByUserId(user.getId()).orElse(null);
            if (config == null || !config.getReminderEnabled()) {
                continue;
            }

            BabyRecord latestFeed = babyRecordRepository.findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(user.getId(), "feed");
            BabyRecord latestDiaper = babyRecordRepository.findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(user.getId(), "diaper");

            LocalDateTime now = LocalDateTime.now();

            if (latestFeed != null) {
                long feedMinutes = Duration.between(latestFeed.getRecordTime(), now).toMinutes();
                if (feedMinutes > config.getFeedIntervalMinutes()) {
                    weChatClient.sendSubscribeMessage(user.getOpenid(), "宝宝喂奶提醒", "已经超过设定时间，记得记录喂奶。 ");
                }
            }

            if (latestDiaper != null) {
                long diaperMinutes = Duration.between(latestDiaper.getRecordTime(), now).toMinutes();
                if (diaperMinutes > config.getDiaperIntervalMinutes()) {
                    weChatClient.sendSubscribeMessage(user.getOpenid(), "宝宝尿便提醒", "已经较长时间未记录尿便情况，记得看看宝宝。 ");
                }
            }
        }
    }
}
