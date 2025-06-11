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

@Controller
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user-profile")
    public String showUserProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {
            model.addAttribute("fullname", sessionUser.getFullName());
            model.addAttribute("email", sessionUser.getEmail());
            model.addAttribute("phone", sessionUser.getPhone());
            model.addAttribute("dob", sessionUser.getDob());
            model.addAttribute("bio", sessionUser.getBio());
            model.addAttribute("gender", sessionUser.getGender());
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

                userService.updateUser(sessionUser);

                session.setAttribute("user", sessionUser); // Cập nhật session
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
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