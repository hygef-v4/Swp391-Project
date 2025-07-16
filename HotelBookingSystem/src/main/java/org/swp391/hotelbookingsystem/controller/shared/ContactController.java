package org.swp391.hotelbookingsystem.controller.shared;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.service.*;

@Controller
public class ContactController {

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/contact")
    public String showContactPage() {
        return "page/contact";
    }

    @PostMapping("/send-message")
    public String sendMessage(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String message,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        Long lastSentTime = (Long) session.getAttribute("lastContactTime");
        long currentTime = System.currentTimeMillis();

        // Nếu chưa gửi bao giờ hoặc đã qua 60 giây thì cho phép gửi
        if (lastSentTime == null || (currentTime - lastSentTime) > 60 * 1000) {
            try {
                emailService.sendContactMessage(name, email, phone, message);
                session.setAttribute("lastContactTime", currentTime); // Cập nhật thời điểm gửi
                redirectAttributes.addFlashAttribute("success", "Gửi thành công! Chúng tôi sẽ phản hồi sớm.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Gửi thất bại. Vui lòng thử lại sau.");
            }
        } else {
            long secondsLeft = 60 - (currentTime - lastSentTime) / 1000;
            redirectAttributes.addFlashAttribute("error", "Vui lòng chờ " + secondsLeft + " giây trước khi gửi lại.");
        }
        return "redirect:/contact";
    }

}
