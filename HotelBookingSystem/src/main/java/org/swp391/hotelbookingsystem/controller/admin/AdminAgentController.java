package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAgentController {

    @GetMapping("/admin-agent-list")
    public String showAgentList(Model model) {

        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalItems", 0);
        model.addAttribute("startIndex", 0);
        model.addAttribute("endIndex", 0);

        return "admin/admin-agent-list";
    }

    @GetMapping("/admin-agent-detail")
    public String showAgentDetail() {
        return "admin/admin-agent-detail";
    }
}
