package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class ForgotPassWordController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        try {
            emailService.sendResetPasswordEmail(email, token);
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
