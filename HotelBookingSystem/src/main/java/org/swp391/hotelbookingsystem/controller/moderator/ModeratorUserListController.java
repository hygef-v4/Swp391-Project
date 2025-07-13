package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.model.Report;
import org.swp391.hotelbookingsystem.service.ReportService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/moderator-user-list")
public class ModeratorUserListController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

    private static final int PAGE_SIZE = 8;

    @GetMapping("")
    public String getUserList(Model model) {
        List<User> userList = userService.getAllUsersWithProfile();
        // Lấy danh sách userId bị flagged (pending report)
        List<Report> pendingReports = reportService.getAllPendingReports();
        java.util.Set<Integer> flaggedUserIds = new java.util.HashSet<>();
        java.util.Map<Integer, String> flagReasons = new java.util.HashMap<>();
        for (Report r : pendingReports) {
            flaggedUserIds.add(r.getReportedUserId());
            flagReasons.put(r.getReportedUserId(), r.getReason());
        }
        // Gắn cờ flagged và flagReason cho user
        for (User u : userList) {
            u.setFlagged(flaggedUserIds.contains(u.getId()));
            u.setFlagReason(flagReasons.get(u.getId()));
        }
        List<User> flaggedUsers = userList.stream().filter(User::isFlagged).toList();
        List<User> nonFlaggedUsers = userList.stream().filter(u -> !u.isFlagged()).toList();
        List<User> sortedUsers = new java.util.ArrayList<>();
        sortedUsers.addAll(flaggedUsers);
        sortedUsers.addAll(nonFlaggedUsers);
        long activeUsers = userList.stream().filter(u -> u.isActive() && !u.isFlagged()).count();
        long lockedUsers = userList.stream().filter(u -> !u.isActive() && !u.isFlagged()).count();
        long flaggedUsersCount = flaggedUsers.size();
        model.addAttribute("users", sortedUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("lockedUsers", lockedUsers);
        model.addAttribute("flaggedUsers", flaggedUsersCount);
        model.addAttribute("flaggedUsersCount", flaggedUsersCount);
        return "moderator/moderatorUserList";
    }

    @PostMapping("/api/moderator/users/{userId}/flag")
    @ResponseBody
    public Map<String, Object> flagUser(@PathVariable int userId, @RequestBody Map<String, String> payload, @SessionAttribute("user") User moderator) {
        Map<String, Object> response = new HashMap<>();
        try {
            String reason = payload.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập lý do báo cáo");
                return response;
            }
            reason = reason.trim();
            if (reason.length() < 10) {
                response.put("success", false);
                response.put("message", "Lý do báo cáo phải có ít nhất 10 ký tự");
                return response;
            }
            if (reason.length() > 200) {
                response.put("success", false);
                response.put("message", "Lý do báo cáo không được vượt quá 200 ký tự");
                return response;
            }
            User user = userService.findUserById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng");
                return response;
            }
            if (reportService.isUserFlagged(userId)) {
                response.put("success", false);
                response.put("message", "Người dùng này đã bị báo cáo trước đó");
                return response;
            }
            if ("ADMIN".equals(user.getRole()) || "MODERATOR".equals(user.getRole())) {
                response.put("success", false);
                response.put("message", "Không thể báo cáo Admin hoặc Moderator");
                return response;
            }
            // Lấy moderatorId từ session
            int moderatorId = moderator.getId();
            reportService.flagUser(moderatorId, userId, reason);
            response.put("success", true);
            response.put("message", "Đã báo cáo người dùng thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi báo cáo người dùng");
        }
        return response;
    }

    @PostMapping("/api/moderator/users/{userId}/unflag")
    @ResponseBody
    public Map<String, Object> unflagUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            reportService.unflagUser(userId);
            response.put("success", true);
            response.put("message", "Đã unflag người dùng thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi unflag người dùng");
        }
        return response;
    }

    @GetMapping("/api/moderator/users/{userId}/details")
    @ResponseBody
    public Map<String, Object> getUserDetails(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findUserById(userId);
            if (user != null) {
                response.put("success", true);
                response.put("user", user);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy thông tin người dùng");
        }
        return response;
    }
}



