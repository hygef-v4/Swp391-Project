package org.swp391.hotelbookingsystem.controller.contact;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ContactPageController {

    private final UserService userService;

    public ContactPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/contact-options")
    public String contactOptions(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get counts for different user types
        int customerCount = userService.getUsersByRole("CUSTOMER").size();
        int hotelOwnerCount = userService.getUsersByRole("HOTEL_OWNER").size();
        int adminCount = userService.getUsersByRole("ADMIN").size();
        int moderatorCount = userService.getUsersByRole("MODERATOR").size();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("hotelOwnerCount", hotelOwnerCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("moderatorCount", moderatorCount);

        return "contact/contact-options";
    }
}
