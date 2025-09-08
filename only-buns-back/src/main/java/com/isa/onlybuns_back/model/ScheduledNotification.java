package com.isa.onlybuns_back.model;

import com.isa.onlybuns_back.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledNotification {
    private final NotificationService notificationService;

    @Autowired
    public ScheduledNotification(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendInactiveUserNotifications() {
        notificationService.notifyInactiveUsers();
    }
}
