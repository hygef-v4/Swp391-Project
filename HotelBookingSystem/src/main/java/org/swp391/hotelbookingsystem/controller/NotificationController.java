package org.swp391.hotelbookingsystem.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Notification service is running"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            int unreadCount = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(Map.of("count", unreadCount));
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestNotification(
            @RequestParam(defaultValue = "message") String type,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            switch (type) {
                case "booking":
                    notificationService.notifyBookingConfirmation(user.getId(), "BK123", "Hotel Test");
                    break;
                case "message":
                    notificationService.notifyNewMessage(user.getId(), "Test Sender", 999);
                    break;
                case "refund":
                    notificationService.notifyRefundSuccess(user.getId(), "BK123", 1500000);
                    break;
                case "hotel":
                    notificationService.notifyHotelApproval(user.getId(), "Hotel Test");
                    break;
                case "promotion":
                    notificationService.notifyPromotion(user.getId(), "Summer Sale", "50%");
                    break;
                default:
                    notificationService.notifyUser(user.getId(), "Test notification message");
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Test notification sent", "type", type));
        } catch (Exception e) {
            System.err.println("Error sending test notification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unread,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            // Use filtered method if filters are provided
            if (type != null || unread != null) {
                Map<String, Object> response = notificationService.getUserNotificationsFiltered(user.getId(), page, size, type, unread);
                
                return ResponseEntity.ok(response);
            } else {
                // Fallback to old method for backward compatibility
                // Use filtered method without any filters to get accurate total count
                Map<String, Object> response = notificationService.getUserNotificationsFiltered(user.getId(), page, size, null, null);
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("Error getting notifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable int notificationId,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            notificationService.markAsRead(user.getId(), notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable int notificationId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            notificationService.deleteNotification(user.getId(), notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String,Object>> deleteAll(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        try {
            notificationService.deleteAllNotifications(user.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
} 