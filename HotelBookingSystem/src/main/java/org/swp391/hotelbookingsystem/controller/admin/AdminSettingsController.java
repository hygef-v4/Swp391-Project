package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.model.SiteSettings;
import org.swp391.hotelbookingsystem.service.SiteSettingsService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swp391.hotelbookingsystem.service.AdminLogService;
import org.springframework.util.StringUtils;
import org.springframework.dao.DataAccessException;
import org.swp391.hotelbookingsystem.model.User;

@Controller
public class AdminSettingsController {
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsController.class);

    @Autowired
    private SiteSettingsService settingsService;

    @Autowired
    private AdminLogService adminLogService;

    // Hiển thị trang cài đặt
    @GetMapping("/admin/website-settings")
    public String showSettings(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        model.addAttribute("logs", adminLogService.getRecentLogs());
        return "admin/admin-settings";
    }

    // Xử lý cập nhật cài đặt
    @PostMapping("/admin/website-settings")
    public String updateSettings(@ModelAttribute SiteSettings settings, Model model, HttpSession session) {
        // Validation
        StringBuilder error = new StringBuilder();
        if (!StringUtils.hasText(settings.getSiteName())) {
            error.append("Tên website không được để trống. ");
        }
        if (!StringUtils.hasText(settings.getSupportEmail())) {
            error.append("Email hỗ trợ không được để trống. ");
        } else if (!settings.getSupportEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            error.append("Email hỗ trợ không hợp lệ. ");
        }
        if (error.length() > 0) {
            model.addAttribute("settings", settings);
            model.addAttribute("error", error.toString());
            model.addAttribute("logs", adminLogService.getRecentLogs());
            return "admin/admin-settings";
        }
        try {
            settingsService.updateSettings(settings);
        } catch (DataAccessException ex) {
            model.addAttribute("settings", settings);
            model.addAttribute("error", "Lỗi khi lưu cài đặt: " + ex.getMessage());
            model.addAttribute("logs", adminLogService.getRecentLogs());
            return "admin/admin-settings";
        }
        User user = (User) session.getAttribute("user");
        String admin = (user != null) ? user.getFullName() + " (" + user.getEmail() + ")" : "unknown";
        String action = "Cập nhật cài đặt hệ thống";
        logger.info("Admin [{}] {}: {}", admin, action, settings);
        adminLogService.log(admin, action, settings.toString());
        model.addAttribute("settings", settings);
        model.addAttribute("success", true);
        model.addAttribute("logs", adminLogService.getRecentLogs());
        return "admin/admin-settings";
    }
} 