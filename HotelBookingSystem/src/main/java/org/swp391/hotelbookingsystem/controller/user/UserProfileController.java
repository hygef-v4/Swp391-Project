package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import java.util.List;

@Controller
public class UserProfileController {

    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public UserProfileController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/user-profile")
    public String showUserProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes,
                                @RequestParam(required = false) String incomplete,
                                @RequestParam(required = false) String reason) {
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {
            model.addAttribute("fullname", sessionUser.getFullName());
            model.addAttribute("email", sessionUser.getEmail());
            model.addAttribute("phone", sessionUser.getPhone());
            model.addAttribute("dob", sessionUser.getDob());
            model.addAttribute("bio", sessionUser.getBio());
            model.addAttribute("gender", sessionUser.getGender());

            // Handle profile completeness validation messages
            if ("true".equals(incomplete) && "hotel_registration".equals(reason)) {
                String validationMessage = (String) session.getAttribute("profileValidationMessage");
                @SuppressWarnings("unchecked")
                List<String> missingFields = (List<String>) session.getAttribute("missingFields");

                if (validationMessage != null) {
                    model.addAttribute("profileIncompleteWarning", validationMessage);
                    model.addAttribute("missingFields", missingFields);
                    model.addAttribute("redirectReason", "hotel_registration");

                    // Check if payment information is missing
                    boolean needsPaymentInfo = missingFields != null &&
                        missingFields.stream().anyMatch(field -> field.contains("thanh toán"));
                    model.addAttribute("needsPaymentInfo", needsPaymentInfo);

                    // Clear session attributes after use
                    session.removeAttribute("profileValidationMessage");
                    session.removeAttribute("missingFields");
                    session.removeAttribute("redirectReason");
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập. Vui lòng đăng nhập để truy cập thông tin cá nhân.");
            return "redirect:/login";
        }

        return "page/userProfile";
    }

    @PostMapping("/update-user-profile")
    public String updateUserProfile(@RequestParam("fullname") String fullname,
                                    @RequestParam("phone") String phone,
                                    @RequestParam("dob") String dob,
                                    @RequestParam("bio") String bio,
                                    @RequestParam("gender") String gender,
                                    @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                    HttpSession session, RedirectAttributes redirectAttributes) {

        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {

            try {
                // Gọi validate từ UserService
                userService.validateUserProfile(fullname, phone, dob, bio);

                // Cập nhật thông tin người dùng nếu hợp lệ
                sessionUser.setFullName(fullname);
                sessionUser.setPhone(phone);
                sessionUser.setDob(java.sql.Date.valueOf(dob));
                sessionUser.setBio(bio);
                sessionUser.setGender(gender);
                // Xử lý cập nhật avatar:
                if (avatarUrl != null) {
                    if (avatarUrl.isBlank()) {
                        sessionUser.setAvatarUrl(null); // Xoá avatar nếu giá trị rỗng
                    } else if (!avatarUrl.equals(sessionUser.getAvatarUrl())) {
                        sessionUser.setAvatarUrl(avatarUrl); // Cập nhật avatar mới nếu khác avatar hiện tại
                    }
                }

                userService.updateUser(sessionUser);

                session.setAttribute("user", sessionUser); // Cập nhật session
                notificationService.notifyProfileUpdate(sessionUser.getId());

                // Check if user was redirected from hotel registration
                String returnTo = (String) session.getAttribute("returnToAfterProfileUpdate");
                if ("hotel_registration".equals(returnTo)) {
                    session.removeAttribute("returnToAfterProfileUpdate");

                    // Validate profile completeness again
                    UserService.ProfileValidationResult validationResult = userService.validateProfileCompleteness(sessionUser);
                    if (validationResult.isComplete()) {
                        // Set flag to skip validation on next register-host access
                        session.setAttribute("profileJustUpdated", "true");
                        redirectAttributes.addFlashAttribute("success", "Hồ sơ đã được hoàn thiện! Bạn có thể tiếp tục đăng ký khách sạn.");
                        return "redirect:/register-host?from=profile";
                    } else {
                        redirectAttributes.addFlashAttribute("warning", "Hồ sơ vẫn chưa đầy đủ. " + validationResult.getMessage());
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
                }
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật thông tin. Vui lòng thử lại.");
            }

        } else {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập. Không thể cập nhật thông tin.");
        }

        return "redirect:/user-profile";
    }
}