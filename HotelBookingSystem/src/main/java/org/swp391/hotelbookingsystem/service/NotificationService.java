package org.swp391.hotelbookingsystem.service;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyUser(int userId, String message) {
        int notificationId = notificationRepository.createNotification(message, false);
        notificationRepository.assignNotificationToUser(userId, notificationId);
    }
}
