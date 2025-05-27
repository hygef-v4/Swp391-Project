package org.swp391.hotelbookingsystem.controller;

import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpdatePasswordController {

    @Autowired
    private UserRepo userRepo; // Sử dụng UserRepo để thao tác với cơ sở dữ liệu

    @Autowired
    private PasswordEncoder passwordEncoder; // Mã hóa mật khẩu

    private static final String USER_PROFILE_PAGE = "page/userProfile";
    private static final String ERROR_OLD_PASSWORD_MISMATCH = "Mật khẩu hiện tại không chính xác.";
    private static final String ERROR_NEW_PASSWORD_MISMATCH = "Mật khẩu mới và mật khẩu xác nhận không khớp.";
    private static final String SUCCESS_PASSWORD_UPDATE = "Cập nhật mật khẩu thành công!";

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User user = userRepo.findByEmail(currentUserEmail);

        if (user == null) {
            return handleError(model, "Không tìm thấy người dùng!");
        }

        if (!isCurrentPasswordValid(currentPassword, user)) {
            return handleError(model, ERROR_OLD_PASSWORD_MISMATCH);
        }

        if (!isNewPasswordValid(newPassword, confirmPassword)) {
            return handleError(model, ERROR_NEW_PASSWORD_MISMATCH);
        }

        updateUserPassword(user, newPassword);
        model.addAttribute("success", SUCCESS_PASSWORD_UPDATE);
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

    private String handleError(Model model, String errorMessage) {
        model.addAttribute("error", errorMessage);
        return USER_PROFILE_PAGE;
    }
}