package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class UpdatePasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam(value = "currentPassword", required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User sessionUser = (User) session.getAttribute("user");

        try {
            // Validate mật khẩu
            userService.validatePasswordChange(sessionUser, currentPassword, newPassword, confirmPassword);

            // Cập nhật mật khẩu mới
            userService.updateUserPassword(sessionUser, newPassword);

            // Thay đổi trong session
            session.setAttribute("user", sessionUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật mật khẩu thành công!");
        } catch (IllegalArgumentException e) {
            // Thông báo lỗi nếu validate thất bại
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Xử lý các lỗi khác
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật mật khẩu. Vui lòng thử lại.");
        }

        return "redirect:/user-profile";
    }


}