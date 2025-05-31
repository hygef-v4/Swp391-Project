package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String showUserProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser != null) {
            // Nếu tồn tại user trong session, đẩy thông tin sang giao diện
            model.addAttribute("fullname", sessionUser.getFullname());
            model.addAttribute("phone", sessionUser.getPhone());
        } else {
            // Trường hợp user không tồn tại, lấy thông tin từ xác thực người dùng
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                User user = userRepo.findByEmail(email);

                if (user != null) {
                    // Lưu thông tin user vào session
                    session.setAttribute("loggedInUser", user);
                    model.addAttribute("fullname", user.getFullname());
                    model.addAttribute("phone", user.getPhone());
                } else {
                    redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại.");
                }
            }
        }

        model.addAttribute("pageTitle", "User Profile");
        return "page/userProfile";

    }

    // Cập nhật thông tin người dùng
    @PostMapping("/update-user-profile")
    public String updateUserProfile(@RequestParam("fullname") String fullname,
                                    @RequestParam("phone") String phone,
                                    Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            User user = userRepo.findByEmail(email);

            if (user != null) {
                // Cập nhật thông tin người dùng
                user.setFullname(fullname);
                user.setPhone(phone);
                userRepo.updateUser(user);

                // Lưu thông tin đã cập nhật vào session
                session.setAttribute("loggedInUser", user);

                // Thêm thông báo thành công vào Flash Attributes
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Bạn không được phép thực hiện thao tác này.");
        }
        return "redirect:/user-profile";
    }

}
