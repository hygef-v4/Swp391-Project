package org.swp391.hotelbookingsystem.service;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    // Basic notification (backward compatibility)
    public void notifyUser(int userId, String message) {
        createNotification(userId, "Thông báo", message, "system", "normal", null, null, null);
    }

    // Enhanced notification method
    public void createNotification(int userId, String title, String message, String type, 
                                 String priority, String actionUrl, String icon, Map<String, Object> metadata) {
        try {
            // Create notification in database
            int notificationId = notificationRepository.createNotification(title, message, type, priority, actionUrl, icon, metadata, false);
            notificationRepository.assignNotificationToUser(userId, notificationId);
            
            // Send real-time notification via WebSocket
            sendRealTimeNotification(userId, notificationId, title, message, type, priority, actionUrl, icon);
            
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Specific notification types for hotel booking system
    public void notifyBookingConfirmation(int userId, String bookingId, String hotelName) {
        String title = "Đặt phòng thành công! 🎉";
        String message = "Bạn đã đặt thành công phòng tại " + hotelName + ". Mã đặt phòng: " + bookingId;
        String actionUrl = "/bookingDetail?bookingId=" + bookingId;
        createNotification(userId, title, message, "booking", "high", actionUrl, "bi-calendar-check", 
                         Map.of("bookingId", bookingId, "hotelName", hotelName));
    }

    public void notifyNewMessage(int userId, String senderName, int senderId) {
        String title = "Tin nhắn mới 💬";
        String message = senderName + " đã gửi tin nhắn cho bạn";
        String actionUrl = "/chat?userId=" + senderId;
        createNotification(userId, title, message, "chat", "normal", actionUrl, "bi-chat-dots",
                         Map.of("senderId", senderId, "senderName", senderName));
    }

    // Enhanced chat notification with merging capability
    public void createOrUpdateChatNotification(int receiverId, String senderName, int senderId, 
                                             String lastMessage, String actionUrl) {
        try {
            // Check if there's an existing unread chat notification from this sender
            Integer existingNotificationId = notificationRepository.findExistingChatNotification(
                receiverId, senderId);
            
            String title = "Tin nhân mới 💬";
            String message = senderName + " đã gửi tin nhân: " + 
                            (lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage);
            
            Map<String, Object> metadata = Map.of(
                "senderId", senderId,
                "senderName", senderName,
                "lastMessage", lastMessage
            );
            
            if (existingNotificationId != null) {
                // Update existing notification with new message content
                notificationRepository.updateNotification(existingNotificationId, title, message, actionUrl, metadata);
                
                // Send real-time notification via WebSocket for the updated notification
                sendRealTimeNotification(receiverId, existingNotificationId, title, message, 
                                       "chat", "normal", actionUrl, "bi-chat-dots");
                
            } else {
                // Create new notification
                int notificationId = notificationRepository.createNotification(title, message, "chat", "normal", 
                                                                             actionUrl, "bi-chat-dots", metadata, false);
                notificationRepository.assignNotificationToUser(receiverId, notificationId);
                
                // Send real-time notification via WebSocket
                sendRealTimeNotification(receiverId, notificationId, title, message, 
                                       "chat", "normal", actionUrl, "bi-chat-dots");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating/updating chat notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyPaymentSuccess(int userId, String bookingId, double amount) {
        String title = "Thanh toán thành công ✅";
        String message = "Thanh toán " + String.format("%,.0f", amount) + "₫ cho đơn đặt phòng " + bookingId + " đã hoàn tất";
        String actionUrl = "/bookingHistory";
        createNotification(userId, title, message, "payment", "high", actionUrl, "bi-credit-card",
                         Map.of("bookingId", bookingId, "amount", amount));
    }

    public void notifyHotelApproval(int userId, String hotelName) {
        String title = "Khách sạn được phê duyệt 🏨";
        String message = "Khách sạn \"" + hotelName + "\" đã được phê duyệt và có thể nhận khách";
        String actionUrl = "/host-listing";
        createNotification(userId, title, message, "hotel", "high", actionUrl, "bi-building",
                         Map.of("hotelName", hotelName));
    }

    public void notifyPromotion(int userId, String promoTitle, String discount) {
        String title = "Ưu đãi đặc biệt! 🎁";
        String message = promoTitle + " - Giảm " + discount;
        String actionUrl = "/hotel-list";
        createNotification(userId, title, message, "promotion", "normal", actionUrl, "bi-gift",
                         Map.of("discount", discount));
    }

    public void notifyHotelAdded(int userId, String hotelName, int hotelId) {
        String title = "Tạo khách sạn thành công 🏨";
        String message = "Bạn đã tạo khách sạn \"" + hotelName + "\" thành công. Khách sạn đang chờ moderator phê duyệt.";
        String actionUrl = "/host-listing";
        createNotification(userId, title, message, "hotel", "normal", actionUrl, "bi-building", Map.of("hotelId", hotelId, "hotelName", hotelName));
        
        // Notify all moderators about the new hotel pending review
        notifyModeratorsNewHotelPending(hotelName, hotelId, userService.findUserById(userId).getFullName());
    }

    public void notifyModeratorsNewHotelPending(String hotelName, int hotelId, String hostName) {
        String title = "Khách sạn mới chờ duyệt 🏨";
        String message = "Khách sạn \"" + hotelName + "\" của " + hostName + " đang chờ phê duyệt.";
        String actionUrl = "/moderator-hotel-list";
        
        // Get all moderators
        List<User> moderators = userService.getUsersByRole("MODERATOR");
        
        // Notify each moderator
        for (User moderator : moderators) {
            createNotification(moderator.getId(), title, message, "pending_review", "high", actionUrl, "bi-clock-history",
                             Map.of("hotelId", hotelId, "hotelName", hotelName, "hostName", hostName));
        }
    }

    public void notifyRoomAdded(int userId, String roomTitle, int hotelId) {
        String title = "Thêm phòng mới thành công 🛏️";
        String message = "Bạn đã thêm phòng \"" + roomTitle + "\" thành công.";
        String actionUrl = "/manage-hotel?hotelId=" + hotelId;
        createNotification(userId, title, message, "hotel", "normal", actionUrl, "bi-door-open", Map.of("hotelId", hotelId, "roomTitle", roomTitle));
    }

    public void notifyHostRegistrationSuccess(int userId) {
        String title = "Đăng ký chủ khách sạn thành công 🏨";
        String message = "Chúc mừng! Bạn đã trở thành chủ khách sạn. Hãy tạo khách sạn đầu tiên của bạn.";
        String actionUrl = "/host-dashboard";
        createNotification(userId, title, message, "system", "high", actionUrl, "bi-award", null);
    }

    // Get user notifications with pagination
    public List<Map<String, Object>> getUserNotifications(int userId, int page, int size) {
        return notificationRepository.getUserNotifications(userId, page, size);
    }

    // Get user notifications with filtering and pagination
    public Map<String, Object> getUserNotificationsFiltered(int userId, int page, int size, String type, Boolean unread) {
        List<Map<String, Object>> notifications = notificationRepository.getUserNotificationsFiltered(userId, page, size, type, unread);
        int totalElements = notificationRepository.getNotificationCount(userId, type, unread);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return Map.of(
            "content", notifications,
            "number", page,
            "size", size,
            "totalElements", totalElements,
            "totalPages", totalPages,
            "first", page == 0,
            "last", page >= totalPages - 1
        );
    }

    // Get unread count
    public int getUnreadCount(int userId) {
        return notificationRepository.getUnreadCount(userId);
    }

    // Mark notification as read
    public void markAsRead(int userId, int notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
        
        // Send real-time update for unread count
        int newUnreadCount = getUnreadCount(userId);
        messagingTemplate.convertAndSend("/topic/notifications." + userId, 
                         Map.of("type", "unread_count_update", "count", newUnreadCount));
    }

    // Mark all as read
    public void markAllAsRead(int userId) {
        notificationRepository.markAllAsRead(userId);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/notifications." + userId,
                         Map.of("type", "unread_count_update", "count", 0));
    }

    // Delete a notification for a user
    public void deleteNotification(int userId, int notificationId) {
        notificationRepository.deleteUserNotification(userId, notificationId);
        // Send real-time update for counts and possibly UI removal
        int newUnread = getUnreadCount(userId);
        messagingTemplate.convertAndSend("/topic/notifications." + userId,
                Map.of("type", "notification_deleted", "id", notificationId));
        messagingTemplate.convertAndSend("/topic/notifications." + userId,
                Map.of("type", "unread_count_update", "count", newUnread));
    }

    // Delete all notifications for user
    public void deleteAllNotifications(int userId) {
        notificationRepository.deleteAllNotificationsForUser(userId);
        // Notify via websocket
        messagingTemplate.convertAndSend("/topic/notifications." + userId,
                Map.of("type", "unread_count_update", "count", 0));
    }

    // Send real-time notification via WebSocket
    private void sendRealTimeNotification(int userId, int notificationId, String title, String message, 
                                        String type, String priority, String actionUrl, String icon) {
        try {
            Map<String, Object> notification = Map.of(
                "notification_id", notificationId,
                "title", title,
                "message", message,
                "notification_type", type,
                "priority", priority,
                "action_url", actionUrl != null ? actionUrl : "",
                "icon", icon != null ? icon : "bi-bell",
                "created_at", System.currentTimeMillis()
            );
            
            // Send to specific user
            messagingTemplate.convertAndSend("/topic/notifications." + userId, 
                                           Map.of("type", "new_notification", "notification", notification));
            
            // Update unread count
            int unreadCount = getUnreadCount(userId);
            messagingTemplate.convertAndSend("/topic/notifications." + userId,
                                           Map.of("type", "unread_count_update", "count", unreadCount));
            
        } catch (Exception e) {
            System.err.println("Error sending real-time notification: " + e.getMessage());
        }
    }
}
