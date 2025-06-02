package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.service.EmailService;

@Controller
public class OtpController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("error", "Session expired. Please register again.");
            return "page/register";
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(user.getEmail(), otp);
        userRepo.saveEmailOtpToken(user.getEmail(), otp);

        model.addAttribute("message", "The OTP has been resent to your email.");
        return "page/verify-email-otp";
    }

}
