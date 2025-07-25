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
        String validationError = validateSettings(settings);
        if (validationError != null) {
            return handleError(model, settings, validationError);
        }

        // Check if settings have actually changed
        SiteSettings currentSettings = settingsService.getSettings();
        if (areSettingsUnchanged(currentSettings, settings)) {
            return handleWarning(model, settings, "Không có thay đổi nào để cập nhật.");
        }

        // Update settings
        try {
            settingsService.updateSettings(settings);
            logSettingsUpdate(session, settings);
            return handleSuccess(model, settings);
        } catch (DataAccessException ex) {
            return handleError(model, settings, "Lỗi khi lưu cài đặt: " + ex.getMessage());
        }
    }

    /**
     * Validate settings input with comprehensive length validation
     */
    private String validateSettings(SiteSettings settings) {
        StringBuilder error = new StringBuilder();

        // Validate site name
        if (!StringUtils.hasText(settings.getSiteName())) {
            error.append("Tên website không được để trống. ");
        } else {
            String siteName = settings.getSiteName().trim();
            if (siteName.length() < 3) {
                error.append("Tên website phải có ít nhất 3 ký tự. ");
            } else if (siteName.length() > 100) {
                error.append("Tên website không được vượt quá 100 ký tự. ");
            } else if (!siteName.matches("^[a-zA-ZÀ-ỹ0-9\\s\\-\\.]+$")) {
                error.append("Tên website chỉ được chứa chữ cái, số, dấu cách, dấu gạch ngang và dấu chấm. ");
            }
        }

        // Validate support email
        if (!StringUtils.hasText(settings.getSupportEmail())) {
            error.append("Email hỗ trợ không được để trống. ");
        } else {
            String email = settings.getSupportEmail().trim();
            if (email.length() > 255) {
                error.append("Email không được vượt quá 255 ký tự. ");
            } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                error.append("Vui lòng nhập địa chỉ email hợp lệ. ");
            }
        }

        // Validate contact phone (optional)
        if (StringUtils.hasText(settings.getContactPhone())) {
            String phone = settings.getContactPhone().trim();
            if (phone.length() < 10) {
                error.append("Số điện thoại phải có ít nhất 10 ký tự. ");
            } else if (phone.length() > 15) {
                error.append("Số điện thoại không được vượt quá 15 ký tự. ");
            } else if (!phone.matches("^(\\+84|84|0)?[0-9]{9,14}$")) {
                error.append("Số điện thoại không hợp lệ. Định dạng: +84xxxxxxxxx, 84xxxxxxxxx hoặc 0xxxxxxxxx. ");
            }
        }

        // Validate contact address (optional)
        if (StringUtils.hasText(settings.getContactAddress())) {
            String address = settings.getContactAddress().trim();
            if (address.length() > 500) {
                error.append("Địa chỉ không được vượt quá 500 ký tự. ");
            }
        }

        return error.length() > 0 ? error.toString() : null;
    }

    /**
     * Handle error response
     */
    private String handleError(Model model, SiteSettings settings, String errorMessage) {
        model.addAttribute("settings", settings);
        model.addAttribute("error", errorMessage);
        addLogDataToModel(model);
        return "admin/admin-settings";
    }

    /**
     * Handle warning response
     */
    private String handleWarning(Model model, SiteSettings settings, String warningMessage) {
        model.addAttribute("settings", settings);
        model.addAttribute("warning", warningMessage);
        addLogDataToModel(model);
        return "admin/admin-settings";
    }

    /**
     * Handle success response
     */
    private String handleSuccess(Model model, SiteSettings settings) {
        model.addAttribute("settings", settings);
        model.addAttribute("success", true);
        addLogDataToModel(model);
        return "admin/admin-settings";
    }

    /**
     * Add log data to model (eliminates code duplication)
     */
    private void addLogDataToModel(Model model) {
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
    }

    /**
     * Log settings update
     */
    private void logSettingsUpdate(HttpSession session, SiteSettings settings) {
        User user = (User) session.getAttribute("user");
        String admin = (user != null) ? user.getFullName() + " (" + user.getEmail() + ")" : "unknown";
        String action = "Cập nhật cài đặt hệ thống";
        logger.info("Admin [{}] {}: {}", admin, action, settings);
        adminLogService.log(admin, action, settings.toString());
    }

    /**
     * Helper method to check if settings have changed
     */
    private boolean areSettingsUnchanged(SiteSettings current, SiteSettings updated) {
        if (current == null || updated == null) {
            return false;
        }

        // Compare site name
        if (!java.util.Objects.equals(
            normalizeString(current.getSiteName()),
            normalizeString(updated.getSiteName()))) {
            return false;
        }

        // Compare support email
        if (!java.util.Objects.equals(
            normalizeString(current.getSupportEmail()),
            normalizeString(updated.getSupportEmail()))) {
            return false;
        }

        // Compare contact phone
        if (!java.util.Objects.equals(
            normalizeString(current.getContactPhone()),
            normalizeString(updated.getContactPhone()))) {
            return false;
        }

        // Compare contact address
        if (!java.util.Objects.equals(
            normalizeString(current.getContactAddress()),
            normalizeString(updated.getContactAddress()))) {
            return false;
        }

        return true; // All fields are the same
    }

    /**
     * Helper method to normalize strings for comparison (trim and handle nulls)
     */
    private String normalizeString(String str) {
        return str == null ? "" : str.trim();
    }
}