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

    // Specific notification types for hotel booking system
    public void notifyBookingConfirmation(int userId, String bookingId, String hotelName) {
        String title = "Đặt phòng thành công! 🎉";
        String message = "Bạn đã đặt thành công phòng tại " + hotelName + ". Mã đặt phòng: " + bookingId;
        String actionUrl = "/bookingHistory";
        createNotification(userId, title, message, "booking", "high", actionUrl, "bi-calendar-check", 
                         Map.of("bookingId", bookingId, "hotelName", hotelName));
    }

    public void notifyNewMessage(int userId, String senderName, int senderId) {
        String title = "Tin nhắn mới 💬";
        String message = senderName + " đã gửi tin nhắn cho bạn";
        
        // Determine the correct action URL based on receiver's role
        String actionUrl;
        try {
            User receiver = userService.findUserById(userId);
            if (receiver != null && receiver.getRole().equals("HOTEL_OWNER")) {
                // Host receives message from customer
                actionUrl = "/host-customer-detail?customerId=" + senderId;
            } else {
                // Customer receives message from host  
                actionUrl = "/customer-host-detail?hostId=" + senderId;
            }
        } catch (Exception e) {
            // Fallback to generic chat URL if unable to determine role
            actionUrl = "/chat?userId=" + senderId;
        }
        
        createNotification(userId, title, message, "chat", "normal", actionUrl, "bi-chat-dots",
                         Map.of("senderId", senderId, "senderName", senderName));
    }

    public void notifyRejectBooking(int userId, String bookingId, double amount){
        String title = "Đặt phòng đã bị hủy ❌";
        String message = "Hoàn " + String.format("%,.0f", amount) + "₫ về tài khoản cua bạn";
        String actionUrl = "/bookingHistory?tab=cancelled";
        createNotification(userId, title, message, "refund", "high", actionUrl, "bi-trash",
                         Map.of("bookingId", bookingId, "amount", amount));
    }

    public void notifyRefundSuccess(int userId, String bookingId, double amount) {
        String title = "Hoàn tiền thành công ✅";
        String message = "Hoàn " + String.format("%,.0f", amount) + "₫ về tài khoản cua bạn";
        String actionUrl = "/bookingHistory?tab=cancelled";
        createNotification(userId, title, message, "refund", "high", actionUrl, "bi-credit-card",
                         Map.of("bookingId", bookingId, "amount", amount));
    }

    public void notifyHotelApproval(int userId, String hotelName) {
        String title = "Khách sạn được phê duyệt 🛎️";
        String message = "Khách sạn \"" + hotelName + "\" đã được phê duyệt và có thể nhận khách";
        String actionUrl = "/host-listing";
        createNotification(userId, title, message, "hotel", "high", actionUrl, "bi-patch-check",
                         Map.of("hotelName", hotelName));
    }

    public void notifyPromotion(int userId, String promoTitle, String discount) {
        String title = "Ưu đãi đặc biệt! 🎁";
        String message = promoTitle + " - Giảm " + discount;
        String actionUrl = "/hotel-list";
        createNotification(userId, title, message, "promotion", "normal", actionUrl, "bi-gift",
                         Map.of("promoTitle", promoTitle, "discount", discount));
    }

    public void notifyHotelAdded(int userId, String hotelName, int hotelId) {
        String title = "Tạo khách sạn thành công 🏨";
        String message = "Bạn đã tạo khách sạn \"" + hotelName + "\" thành công. Khách sạn đang chờ moderator phê duyệt.";
        String actionUrl = "/host-listing";
        createNotification(userId, title, message, "hotel", "normal", actionUrl, "bi-building", 
                         Map.of("hotelId", hotelId, "hotelName", hotelName));
    }

    public void notifyRoomAdded(int userId, String roomTitle, int hotelId) {
        String title = "Thêm phòng mới thành công 🛏️";
        String message = "Bạn đã thêm phòng \"" + roomTitle + "\" thành công.";
        String actionUrl = "/manage-hotel?hotelId=" + hotelId;
        createNotification(userId, title, message, "hotel", "normal", actionUrl, "bi-door-open",
                         Map.of("hotelId", hotelId, "roomTitle", roomTitle));
    }

    public void notifyHostRegistrationSuccess(int userId) {
        String title = "Đăng ký chủ khách sạn thành công 🔰";
        String message = "Chúc mừng! Bạn đã trở thành chủ khách sạn. Hãy tạo khách sạn đầu tiên của bạn.";
        String actionUrl = "/host-dashboard";
        createNotification(userId, title, message, "system", "high", actionUrl, "bi-award", null);
    }

    public void notifyWishlistAdd(int userId, String hotelName, int hotelId) {
        String title = "Đã thêm vào yêu thích ❤️";
        String message = "Bạn đã thêm khách sạn '" + hotelName + "' vào danh sách yêu thích.";
        String actionUrl = "/user-wishlist";
        createNotification(userId, title, message, "wishlist", "normal", actionUrl, "bi-heart",
                         Map.of("hotelId", hotelId, "hotelName", hotelName));
    }

    public void notifyWishlistRemove(int userId, String hotelName, int hotelId) {
        String title = "Đã xoá khỏi yêu thích 💔";
        String message = "Bạn đã xoá khách sạn '" + hotelName + "' khỏi danh sách yêu thích.";
        String actionUrl = "/user-wishlist";
        createNotification(userId, title, message, "wishlist", "normal", actionUrl, "bi-heartbreak",
                         Map.of("hotelId", hotelId, "hotelName", hotelName));
    }

    public void notifyProfileUpdate(int userId) {
        String title = "Cập nhật thông tin cá nhân ✏️";
        String message = "Bạn đã cập nhật thông tin cá nhân thành công.";
        String actionUrl = "/user-profile";
        createNotification(userId, title, message, "profile", "normal", actionUrl, "bi-person-circle", null);
    }

    public void notifyNewReview(int hotelOwnerId, String reviewerName, String hotelName, int rating, int hotelId) {
        String title = "Đánh giá mới cho khách sạn! ⭐";
        String message = reviewerName + " đã đánh giá " + rating + " sao cho khách sạn \"" + hotelName + "\"";
        String actionUrl = "/hotel-detail?hotelId=" + hotelId;
        createNotification(hotelOwnerId, title, message, "review", "normal", actionUrl, "bi-star-fill",
                         Map.of("hotelId", hotelId, "hotelName", hotelName, "rating", rating, "reviewerName", reviewerName));
    }

    public void notifyReviewReply(int reviewerId, String replierName, String hotelName, int hotelId) {
        String title = "Có phản hồi cho đánh giá của bạn! 💬";
        String message = replierName + " đã phản hồi đánh giá của bạn tại khách sạn \"" + hotelName + "\"";
        String actionUrl = "/hotel-detail?hotelId=" + hotelId;
        createNotification(reviewerId, title, message, "reply", "normal", actionUrl, "bi-reply-fill",
                         Map.of("hotelId", hotelId, "hotelName", hotelName, "replierName", replierName));
    }

    public void notifyPasswordChanged(int userId) {
        String title = "Đổi mật khẩu thành công 🛡️";
        String message = "Bạn đã đổi mật khẩu tài khoản thành công. Nếu không phải bạn thực hiện, hãy liên hệ hỗ trợ ngay!";
        String actionUrl = "/user-change-password";
        createNotification(userId, title, message, "profile", "high", actionUrl, "bi-shield-lock", null);
    }

    // Enhanced chat notification method that determines correct action URL based on user roles
    public void createChatNotification(int receiverId, String senderName, int senderId,
                                     String lastMessage, String senderRole, String receiverRole) {
        try {
            // Check if there's an existing unread chat notification from this sender
            Integer existingNotificationId = notificationRepository.findExistingChatNotification(
                receiverId, senderId);

            String title = "Tin nhắn mới 💬";
            String message = senderName + " đã gửi tin nhắn: " +
                            (lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage);

            // Determine the correct action URL based on roles
            String actionUrl = getChatActionUrl(senderRole, receiverRole, senderId, receiverId);

            Map<String, Object> metadata = Map.of(
                "senderId", senderId,
                "senderName", senderName,
                "lastMessage", lastMessage,
                "senderRole", senderRole,
                "receiverRole", receiverRole
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
            System.err.println("Error creating chat notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getChatActionUrl(String senderRole, String receiverRole, int senderId, int receiverId) {
        // Determine action URL based on receiver's role and sender's role
        switch (receiverRole) {
            case "HOTEL_OWNER":
                if ("CUSTOMER".equals(senderRole)) {
                    return "/host-customer-detail?customerId=" + senderId;
                } else if ("ADMIN".equals(senderRole)) {
                    return "/agent-admin-contact?userId=" + senderId;
                } else if ("MODERATOR".equals(senderRole)) {
                    return "/agent-moderator-contact?userId=" + senderId;
                }
                break;

            case "CUSTOMER":
                if ("HOTEL_OWNER".equals(senderRole)) {
                    return "/customer-host-detail?hostId=" + senderId;
                } else if ("MODERATOR".equals(senderRole)) {
                    return "/customer-moderator-contact?userId=" + senderId;
                } else if ("ADMIN".equals(senderRole)) {
                    return "/customer-admin-contact?userId=" + senderId;
                }
                break;

            case "ADMIN":
                if ("HOTEL_OWNER".equals(senderRole)) {
                    return "/admin-agent-contact?userId=" + senderId;
                } else if ("CUSTOMER".equals(senderRole)) {
                    return "/admin-customer-contact?userId=" + senderId;
                } else if ("MODERATOR".equals(senderRole)) {
                    return "/admin-moderator-contact?userId=" + senderId;
                }
                break;

            case "MODERATOR":
                if ("CUSTOMER".equals(senderRole)) {
                    return "/moderator-customer-contact?userId=" + senderId;
                } else if ("HOTEL_OWNER".equals(senderRole)) {
                    return "/moderator-agent-contact?userId=" + senderId;
                } else if ("ADMIN".equals(senderRole)) {
                    return "/moderator-admin-contact?userId=" + senderId;
                }
                break;
        }

        // Fallback URL
        return "/chat?userId=" + senderId;
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
