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
    private static final String ERROR_NEW_PASSWORD_MISMATCH = "Mật khẩu mới và xác nhận mật khẩu không khớp.";
    private static final String SUCCESS_PASSWORD_UPDATE = "Cập nhật mật khẩu thành công!";

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam(value = "currentPassword", required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập!");
            return USER_PROFILE_PAGE;
        }

        if (sessionUser.getPassword() != null && !passwordEncoder.matches(currentPassword, sessionUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác.");
            return USER_PROFILE_PAGE;
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", ERROR_NEW_PASSWORD_MISMATCH);
            return USER_PROFILE_PAGE;
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        sessionUser.setPassword(encodedPassword);
        userRepo.updateUserPassword(sessionUser.getEmail(), encodedPassword);

        session.setAttribute("user", sessionUser);
        redirectAttributes.addFlashAttribute("success", SUCCESS_PASSWORD_UPDATE);
        return USER_PROFILE_PAGE;
    }
}