package org.swp391.hotelbookingsystem.controller.shared;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalPagesController {
    
    @GetMapping("/terms-of-service")
    public String termsOfService(Model model) {
        model.addAttribute("pageTitle", "Điều Khoản Dịch Vụ - Hamora");
        return "page/terms-of-service";
    }
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        model.addAttribute("pageTitle", "Chính Sách Bảo Mật - Hamora");
        return "page/privacy-policy";
    }
}
