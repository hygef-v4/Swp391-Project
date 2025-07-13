package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class ForgotPassWordController {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPassWordController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm() {
        return "page/forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String processForgotPassword(HttpServletRequest request, @RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);

        if (user == null) {
            return "redirect:/forgotPassword?error";
        }

        String token = java.util.UUID.randomUUID().toString();
        userService.savePasswordResetToken(user.getId(), token);

        // Build dynamic base URL using request
        String appUrl = request.getRequestURL()
                .toString()
                .replace(request.getRequestURI(), "");

        // Construct reset link with token
        String resetLink = appUrl + "/resetPassword?token=" + token;

        try {
            emailService.sendResetPasswordEmail(email, resetLink);
        } catch (jakarta.mail.MessagingException e) {
            return "redirect:/forgotPassword?mailerror";
        }

        return "redirect:/forgotPassword?success";
    }

    @GetMapping("/resetPassword")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        User user = userService.findUserByToken(token);
        if (user == null) {
            return "redirect:/forgotPassword?invalid";
        }
        model.addAttribute("token", token);
        return "page/resetPassword";
    }

    @PostMapping("/resetPassword")
    public String processReset(@RequestParam("token") String token,
                               @RequestParam("password") String password) {
        User user = userService.findUserByToken(token);
        if (user == null) {
            return "redirect:/forgotPassword?invalid";
        }

        String hashed = passwordEncoder.encode(password);
        userService.updatePassword(user.getId(), hashed);
        userService.deleteToken(token);

        return "redirect:/login?resetSuccess";
    }

}
