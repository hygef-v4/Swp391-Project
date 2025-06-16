package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminReviewController {
    @GetMapping("/admin-review")
    public String showReviewList() {
        return "admin/admin-review";
    }
}
