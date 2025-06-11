package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModeratorDashboardController {

    @GetMapping("/moderator-dashboard")
    public String getDashboard() {
        return "page/moderatorDashboard";
    }
}
