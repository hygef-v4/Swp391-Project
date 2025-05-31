package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpdatePasswordController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_PROFILE_PAGE = "redirect:/user-profile";
    private static final String ERROR_OLD_PASSWORD_MISMATCH = "Mật khẩu hiện tại không chính xác.";
    private static final String ERROR_NEW_PASSWORD_MISMATCH = "Mật khẩu mới và mật khẩu xác nhận không khớp.";
    private static final String SUCCESS_PASSWORD_UPDATE = "Cập nhật mật khẩu thành công!";

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập!");
            return USER_PROFILE_PAGE;
        }

        if (!isCurrentPasswordValid(currentPassword, sessionUser)) {
            redirectAttributes.addFlashAttribute("error", ERROR_OLD_PASSWORD_MISMATCH);
            return USER_PROFILE_PAGE;
        }

        if (!isNewPasswordValid(newPassword, confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", ERROR_NEW_PASSWORD_MISMATCH);
            return USER_PROFILE_PAGE;
        }

        updateUserPassword(sessionUser, newPassword);

        // Update user in session after password change (if needed)
        session.setAttribute("user", sessionUser);
        redirectAttributes.addFlashAttribute("success", SUCCESS_PASSWORD_UPDATE);
        return USER_PROFILE_PAGE;
    }

    private boolean isCurrentPasswordValid(String currentPassword, User user) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    private boolean isNewPasswordValid(String newPassword, String confirmPassword) {
        return newPassword.equals(confirmPassword);
    }

    private void updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        userRepo.updateUserPassword(user.getEmail(), encodedPassword);
    }
}