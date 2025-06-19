package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;

import java.util.List;

@Controller
public class AdminAgentController {

    private final UserService userService;
    public AdminAgentController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/admin-agent-list")
    public String showAgentList(Model model) {

        List<User> agentList = userService.getUsersByRole("HOTEL_OWNER");

        model.addAttribute("agentList", agentList);
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
