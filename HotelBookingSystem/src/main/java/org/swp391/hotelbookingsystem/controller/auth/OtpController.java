package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpSession;
import org.swp391.hotelbookingsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class OtpController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/verify-email-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        User user = (User) session.getAttribute("register_user");

        if (user == null) {
            model.addAttribute("error", "Session expired. Please register again.");
            return "page/register";
        }

        if (!userService.isValidEmailOtp(user.getEmail(), otp)) {
            model.addAttribute("error", "Invalid or expired OTP.");
            return "page/verify-email-otp";
        }

        userService.saveUser(user);
        userService.deleteEmailOtp(user.getEmail(), otp);
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
        userService.saveEmailOtpToken(user.getEmail(), otp);

        model.addAttribute("message", "The OTP has been resent to your email.");
        return "page/verify-email-otp";
    }

}
