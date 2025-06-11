package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.http.HttpSession;
import org.swp391.hotelbookingsystem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
public class OtpController {

    private final UserService userService;
    private final EmailService emailService;

    public OtpController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/verify-email-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        User user = (User) session.getAttribute("register_user");

        if (user == null) {
            model.addAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng đăng ký lại.");
            return "page/register";
        }

        if (!userService.isValidEmailOtp(user.getEmail(), otp)) {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            return "page/verify-email-otp";
        }

        userService.saveUser(user);
        userService.deleteEmailOtp(user.getEmail(), otp);
        session.invalidate();

        return "redirect:/login";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, Model model) {
        User user = (User) session.getAttribute("register_user");

        if (user == null) {
            model.addAttribute("error", "Phiên làm việc đã hết hạn. Vui long đăng ký lại.");
            return "page/register";
        }

        Long lastSentTime = (Long) session.getAttribute("last_otp_sent_time");
        long currentTime = System.currentTimeMillis();

        if (lastSentTime != null && (currentTime - lastSentTime) < 60_000) {
            model.addAttribute("error", "Vui lòng chờ ít nhất 60 giây trước khi gửi lại OTP.");
            return "page/verify-email-otp";
        }

        userService.deleteEmailOtp(user.getEmail());

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(user.getEmail(), otp);
        userService.saveEmailOtpToken(user.getEmail(), otp);

        session.setAttribute("last_otp_sent_time", currentTime);

        model.addAttribute("message", "Mã OTP đã được gửi lại đến email của bạn.");
        return "page/verify-email-otp";
    }

}
