package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OtpController {

    @Autowired
    private UserRepo userRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/verify-email-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("error", "Session expired. Please register again.");
            return "page/register";
        }

        if (!userRepo.isValidEmailOtp(user.getEmail(), otp)) {
            model.addAttribute("error", "Invalid or expired OTP.");
            return "page/verify-email-otp";
        }

        userRepo.saveUser(user);
        userRepo.deleteEmailOtp(user.getEmail(), otp);
        session.invalidate();

        return "redirect:/login";
    }
}
