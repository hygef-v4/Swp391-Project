package org.swp391.hotelbookingsystem.controller;

import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    public String showUserProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra thông tin xác thực và lấy người dùng hiện tại
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername(); // Lấy email từ thông tin xác thực người dùng
            User user = userRepo.findByEmail(email); // Lấy thông tin người dùng dựa vào email

            if (user != null) {
                // Truyền dữ liệu người dùng (fullname, phone) xuống giao diện
                model.addAttribute("fullname", user.getFullname());
                model.addAttribute("phone", user.getPhone());
            } else {
                model.addAttribute("error", "Người dùng không tồn tại.");
            }
        }
        model.addAttribute("pageTitle", "User Profile");
        return "page/userProfile"; // Định nghĩa giao diện userProfile.html
    }

    @PostMapping("/user-profile/update")
    public String updateUserProfile(@RequestParam("fullname") String fullname,
                                    @RequestParam("phone") String phone,
                                    Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername(); // Lấy email từ người dùng đang đăng nhập
            User user = userRepo.findByEmail(email);

            if (user != null) {
                // Cập nhật thông tin người dùng (fullname và phone)
                user.setFullname(fullname);
                user.setPhone(phone);
                userRepo.updateUser(user);

                // Hiển thị thông báo thành công
                model.addAttribute("success", "Cập nhật thông tin thành công.");
            } else {
                model.addAttribute("error", "Người dùng không tồn tại.");
            }
        } else {
            model.addAttribute("error", "Bạn không được phép thực hiện thao tác này.");
        }

        return "page/userProfile"; // Điều hướng về lại trang userProfile
    }
}