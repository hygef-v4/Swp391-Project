package org.swp391.hotelbookingsystem.controller.shared;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaqController {
    @GetMapping("/faq")
    public String faqPage(Model model) {
        model.addAttribute("pageTitle", "FAQ - Câu hỏi thường gặp | Hamora Booking");
        return "page/faq";
    }
} 