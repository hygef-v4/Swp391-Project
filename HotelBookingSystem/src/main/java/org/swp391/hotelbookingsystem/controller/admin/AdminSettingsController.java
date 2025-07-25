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
import org.swp391.hotelbookingsystem.model.AdminLogEntry;
import java.util.List;

@Controller
public class AdminSettingsController {
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsController.class);

    private final SiteSettingsService settingsService;

    private final AdminLogService adminLogService;

    public AdminSettingsController(SiteSettingsService settingsService, AdminLogService adminLogService) {
        this.settingsService = settingsService;
        this.adminLogService = adminLogService;
    }

    // Hiển thị trang cài đặt
    @GetMapping("/admin/website-settings")
    public String showSettings(Model model,
                              @RequestParam(value = "adminFilter", required = false) String adminFilter,
                              @RequestParam(value = "actionFilter", required = false) String actionFilter,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {

        model.addAttribute("settings", settingsService.getSettings());

        // Get filtered and paginated logs
        List<AdminLogEntry> logs = adminLogService.getFilteredLogs(adminFilter, actionFilter, page, size);
        int totalCount = adminLogService.getTotalFilteredCount(adminFilter, actionFilter);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Add pagination and filter data to model
        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageSize", size);

        // Add filter values back to model for form persistence
        model.addAttribute("adminFilter", adminFilter);
        model.addAttribute("actionFilter", actionFilter);

        // Add unique values for filter dropdowns
        model.addAttribute("uniqueAdmins", adminLogService.getUniqueAdmins());
        model.addAttribute("uniqueActions", adminLogService.getUniqueActions());

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

            // Add default log data for error response
            List<AdminLogEntry> logs = adminLogService.getFilteredLogs(null, null, 1, 10);
            int totalCount = adminLogService.getTotalFilteredCount(null, null);
            int totalPages = (int) Math.ceil((double) totalCount / 10);

            model.addAttribute("logs", logs);
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("pageSize", 10);
            model.addAttribute("uniqueAdmins", adminLogService.getUniqueAdmins());
            model.addAttribute("uniqueActions", adminLogService.getUniqueActions());

            return "admin/admin-settings";
        }
        try {
            settingsService.updateSettings(settings);
        } catch (DataAccessException ex) {
            model.addAttribute("settings", settings);
            model.addAttribute("error", "Lỗi khi lưu cài đặt: " + ex.getMessage());

            // Add default log data for database error response
            List<AdminLogEntry> logs = adminLogService.getFilteredLogs(null, null, 1, 10);
            int totalCount = adminLogService.getTotalFilteredCount(null, null);
            int totalPages = (int) Math.ceil((double) totalCount / 10);

            model.addAttribute("logs", logs);
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("pageSize", 10);
            model.addAttribute("uniqueAdmins", adminLogService.getUniqueAdmins());
            model.addAttribute("uniqueActions", adminLogService.getUniqueActions());

            return "admin/admin-settings";
        }
        User user = (User) session.getAttribute("user");
        String admin = (user != null) ? user.getFullName() + " (" + user.getEmail() + ")" : "unknown";
        String action = "Cập nhật cài đặt hệ thống";
        logger.info("Admin [{}] {}: {}", admin, action, settings);
        adminLogService.log(admin, action, settings.toString());
        model.addAttribute("settings", settings);
        model.addAttribute("success", true);

        // Add default log data for POST response
        List<AdminLogEntry> logs = adminLogService.getFilteredLogs(null, null, 1, 10);
        int totalCount = adminLogService.getTotalFilteredCount(null, null);
        int totalPages = (int) Math.ceil((double) totalCount / 10);

        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageSize", 10);
        model.addAttribute("uniqueAdmins", adminLogService.getUniqueAdmins());
        model.addAttribute("uniqueActions", adminLogService.getUniqueActions());

        return "admin/admin-settings";
    }
} 