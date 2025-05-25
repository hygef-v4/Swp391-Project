package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

@Controller
public class ForgotPassWordController {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm() {
        return "page/forgotPassword";
    }

    // Xử lý form submit
    @PostMapping("/forgotPassword")
    public String processForgotPassword(HttpServletRequest request, @RequestParam("email") String email, Model model) {
        User user = userRepo.findByEmail(email);

        if (user == null) {
            return "redirect:/forgotPassword?error";
        }

        System.out.println("Email hợp lệ: " + email + " (Giả lập gửi link reset)");

        return "redirect:/forgotPassword?success";
    }
}
