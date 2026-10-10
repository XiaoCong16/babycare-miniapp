package com.example.babycare.infrastructure.scheduler;

import com.example.babycare.application.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void checkReminder() {
        reminderService.checkAndSendReminders();
    }
}
