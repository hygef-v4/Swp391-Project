package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UpdatePasswordController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user-change-password")
    public String showUserProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {
            model.addAttribute("passwordNotSet", sessionUser.getPassword() == null);
        } else {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập. Vui lòng đăng nhập để truy cập thông tin cá nhân.");
            return "redirect:/login";
        }

        return "page/changePassword";
    }

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

        return "redirect:/user-change-password";
    }


}