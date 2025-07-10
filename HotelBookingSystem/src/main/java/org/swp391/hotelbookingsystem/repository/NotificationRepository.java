package org.swp391.hotelbookingsystem.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Legacy method for backward compatibility
    public int createNotification(String message, boolean isGlobal) {
        return createNotification("Thông báo", message, "system", "normal", null, null, null, isGlobal);
    }

    // Enhanced notification creation
    public int createNotification(String title, String message, String type, String priority, 
                                String actionUrl, String icon, Map<String, Object> metadata, boolean isGlobal) {
        String sql = "INSERT INTO Notifications (title, message, notification_type, priority, action_url, icon, metadata, is_global, created_at) " +
                    "OUTPUT INSERTED.notification_id VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        
        String metadataJson = metadata != null ? metadata.toString() : null; // Simple JSON conversion
        
        return jdbcTemplate.queryForObject(sql, Integer.class, title, message, type, priority, 
                                          actionUrl, icon, metadataJson, isGlobal ? 1 : 0);
    }

    public void assignNotificationToUser(int userId, int notificationId) {
        String sql = "INSERT INTO UserNotifications (user_id, notification_id, is_read, created_at) VALUES (?, ?, 0, GETDATE())";
        jdbcTemplate.update(sql, userId, notificationId);
    }

    // Get user notifications with pagination
    public List<Map<String, Object>> getUserNotifications(int userId, int page, int size) {
        int offset = page * size;
        String sql = """
            SELECT n.notification_id, n.title, n.message, n.notification_type, n.priority, 
                   n.action_url, n.icon, n.created_at, un.is_read, un.read_at
            FROM Notifications n
            INNER JOIN UserNotifications un ON n.notification_id = un.notification_id
            WHERE un.user_id = ?
            ORDER BY n.created_at DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;
        
        return jdbcTemplate.queryForList(sql, userId, offset, size);
    }

    // Get unread notification count
    public int getUnreadCount(int userId) {
        String sql = """
            SELECT COUNT(*)
            FROM UserNotifications un
            INNER JOIN Notifications n ON un.notification_id = n.notification_id
            WHERE un.user_id = ? AND un.is_read = 0
            AND (n.expires_at IS NULL OR n.expires_at > GETDATE())
            """;
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // Mark specific notification as read
    public void markAsRead(int userId, int notificationId) {
        String sql = "UPDATE UserNotifications SET is_read = 1, read_at = GETDATE() WHERE user_id = ? AND notification_id = ?";
        jdbcTemplate.update(sql, userId, notificationId);
    }

    // Mark all notifications as read for user
    public void markAllAsRead(int userId) {
        String sql = "UPDATE UserNotifications SET is_read = 1, read_at = GETDATE() WHERE user_id = ? AND is_read = 0";
        jdbcTemplate.update(sql, userId);
    }

    // Delete a notification for a specific user
    public void deleteUserNotification(int userId, int notificationId) {
        String sql = "DELETE FROM UserNotifications WHERE user_id = ? AND notification_id = ?";
        jdbcTemplate.update(sql, userId, notificationId);

        // Optionally, clean up the Notifications table if no users reference it anymore
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM UserNotifications WHERE notification_id = ?",
                Integer.class,
                notificationId);
            if (count != null && count == 0) {
                jdbcTemplate.update("DELETE FROM Notifications WHERE notification_id = ?", notificationId);
            }
        } catch (Exception e) {
            // Log and ignore cleanup errors to avoid impacting user operation
            System.err.println("[WARN] Failed to cleanup orphan notification " + notificationId + ": " + e.getMessage());
        }
    }

    // Get notifications by type for user
    public List<Map<String, Object>> getNotificationsByType(int userId, String type, int limit) {
        String sql = """
            SELECT TOP (?) n.notification_id, n.title, n.message, n.notification_type, 
                   n.action_url, n.icon, n.created_at, un.is_read
            FROM Notifications n
            INNER JOIN UserNotifications un ON n.notification_id = un.notification_id
            WHERE un.user_id = ? AND n.notification_type = ?
            ORDER BY n.created_at DESC
            """;
        
        return jdbcTemplate.queryForList(sql, limit, userId, type);
    }

    // Get user notifications with filtering and pagination
    public List<Map<String, Object>> getUserNotificationsFiltered(int userId, int page, int size, String type, Boolean unread) {
        int offset = page * size;
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT n.notification_id, n.title, n.message, n.notification_type, n.priority, 
                   n.action_url, n.icon, n.created_at, un.is_read, un.read_at
            FROM Notifications n
            INNER JOIN UserNotifications un ON n.notification_id = un.notification_id
            WHERE un.user_id = ?
            """);
        
        // Build parameters list
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        // Add type filter if specified
        if (type != null && !type.isEmpty()) {
            sql.append(" AND n.notification_type = ?");
            params.add(type);
        }
        
        // Add unread filter if specified
        if (unread != null && unread) {
            sql.append(" AND un.is_read = 0");
        }
        
        sql.append(" ORDER BY n.created_at DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        // Add pagination parameters
        params.add(offset);
        params.add(size);
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return results;
    }

    // Get total count of notifications with filtering
    public int getNotificationCount(int userId, String type, Boolean unread) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT COUNT(*)
            FROM Notifications n
            INNER JOIN UserNotifications un ON n.notification_id = un.notification_id
            WHERE un.user_id = ?
            """);
        
        // Build parameters list
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        // Add type filter if specified
        if (type != null && !type.isEmpty()) {
            sql.append(" AND n.notification_type = ?");
            params.add(type);
        }
        
        // Add unread filter if specified
        if (unread != null && unread) {
            sql.append(" AND un.is_read = 0");
        }
        
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    // Delete old notifications (cleanup)
    public int deleteExpiredNotifications() {
        String sql = "DELETE FROM Notifications WHERE expires_at IS NOT NULL AND expires_at < GETDATE()";
        return jdbcTemplate.update(sql);
    }
}