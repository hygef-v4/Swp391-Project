package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerForm() {
        return "page/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("fullname") String fullname,
            Model model,
            HttpSession session) {


        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullname.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            return "page/register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "page/register";
        }

        if (userRepo.findByEmail(email) != null) {
            model.addAttribute("error", "Email already exists.");
            return "page/register";
        }

        if (!fullname.matches("^[\\p{L} '\\-]+$")) {
            model.addAttribute("error", "Full name must not contain special characters.");
            return "page/register";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "page/register";
        }

        String passwordPattern = "^(?=.*[A-Z])(?=.*[\\W_]).+$";
        if (!password.matches(passwordPattern)) {
            model.addAttribute("error", "Password must contain at least one uppercase letter and one special character.");
            return "page/register";
        }

        String hashedPassword = passwordEncoder.encode(password);
        User existingUser = userRepo.findByEmail(email);

        if (existingUser != null) {
            model.addAttribute("error", "Email already exists.");
            return "page/register";
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(email, otp);
        userRepo.saveEmailOtpToken(email, otp);

        User user = new User(email, hashedPassword);
        user.setFullname(fullname);
        session.setAttribute("user", user);

        return "page/verify-email-otp";

    }
}
