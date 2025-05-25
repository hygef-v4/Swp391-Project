package org.swp391.hotelbookingsystem.controller;

import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
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
        return "register";
    }

    // Xử lý submit form đăng ký
    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        //Validate đầu vào
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        if (userRepo.findByEmail(email) != null) {
            model.addAttribute("error", "Email already exists.");
            return "register";
        }

        //Mã hóa mật khẩu
        String hashedPassword = passwordEncoder.encode(password);

        //Tạo và lưu người dùng
        User user = new User(email, hashedPassword);
        userRepo.saveUser(user);

        model.addAttribute("email", email);

        //Điều hướng sau khi thành công
        return "redirect:/login";
    }
}
