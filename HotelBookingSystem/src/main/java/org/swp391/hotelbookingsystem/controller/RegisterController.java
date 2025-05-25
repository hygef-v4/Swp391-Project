package org.swp391.hotelbookingsystem.controller;

import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.swp391.hotelbookingsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @Autowired
    private UserRepo userRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/register")
    public String registerForm() {
        return "page/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(email, hashedPassword);
        userRepo.saveUser(user);

        model.addAttribute("email", email);

        return "redirect:/login";
    }
}
