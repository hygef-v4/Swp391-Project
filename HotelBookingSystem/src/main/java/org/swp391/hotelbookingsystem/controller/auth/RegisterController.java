package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class RegisterController {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

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
            model.addAttribute("error", "Vui lòng điền đầy đủ tất cả các trường.");
            return "page/register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu nhập lại không khớp.");
            return "page/register";
        }

        if (userService.findByEmail(email) != null) {
            model.addAttribute("error", "Email đã tồn tại. Vui lòng sử dụng email khác.");
            return "page/register";
        }

        if (!fullname.matches("^[\\p{L} '\\-]+$")) {
            model.addAttribute("error", "Họ và tên không được chứa ký tự đặc biệt hoặc số.");
            return "page/register";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            return "page/register";
        }

        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$";
        if (!password.matches(passwordPattern)) {
            model.addAttribute("error", "Mật khẩu phải chứa ít nhất một chữ cái in hoa, một chữ số và một ký tự đặc biệt.");
            return "page/register";
        }

        String hashedPassword = passwordEncoder.encode(password);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(email, otp);
        userService.saveEmailOtpToken(email, otp);

        User user = new User(email, hashedPassword);
        user.setFullName(fullname.trim());
        session.setAttribute("register_user", user);

        return "page/verify-email-otp";

    }
}
