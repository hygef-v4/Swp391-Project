package org.swp391.hotelbookingsystem.controller.shared;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {
    @GetMapping("/about")
    public String aboutPage() {
        return "page/about";
    }
} 