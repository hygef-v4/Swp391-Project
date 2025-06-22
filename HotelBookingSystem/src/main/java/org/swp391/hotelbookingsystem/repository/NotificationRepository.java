package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createNotification(String message, boolean isGlobal) {
        String sql = "INSERT INTO Notifications (message, is_global, sent_at) OUTPUT INSERTED.notification_id VALUES (?, ?, GETDATE())";
        return jdbcTemplate.queryForObject(sql, Integer.class, message, isGlobal ? 1 : 0);
    }

    public void assignNotificationToUser(int userId, int notificationId) {
        String sql = "INSERT INTO UserNotifications (user_id, notification_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, notificationId);
    }
}