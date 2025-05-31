package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserProfileController {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/user-profile")
    public String showUserProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {
            // Đẩy fullname và phone từ session đối tượng người dùng qua giao diện
            model.addAttribute("fullname", sessionUser.getFullname());
            model.addAttribute("phone", sessionUser.getPhone());
        } else {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập. Vui lòng đăng nhập để truy cập thông tin cá nhân.");
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "User Profile");
        return "page/userProfile";
    }

    // Cập nhật thông tin người dùng
    @PostMapping("/update-user-profile")
    public String updateUserProfile(@RequestParam("fullname") String fullname,
                                    @RequestParam("phone") String phone,
                                    Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null) {
            // Cập nhật thông tin người dùng trong session
            sessionUser.setFullname(fullname);
            sessionUser.setPhone(phone);
            userRepo.updateUser(sessionUser); // Lưu dữ liệu mới vào cơ sở dữ liệu

            // Cập nhật trong session
            session.setAttribute("user", sessionUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa đăng nhập. Không thể cập nhật thông tin.");
        }

        return "redirect:/user-profile";
    }

}