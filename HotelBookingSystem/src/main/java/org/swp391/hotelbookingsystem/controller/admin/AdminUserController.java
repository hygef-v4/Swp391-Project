package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.*;

import java.util.*;

@Controller
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin-user-list")
    public String getUserList(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "role", required = false) String role,
                              Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        List<User> users;

        if (search != null && !search.isBlank() && role != null && !role.isBlank()) {
            users = userService.getAllUsersWithProfile().stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(search.toLowerCase()))
                    .filter(u -> role.equalsIgnoreCase(u.getRole()))
                    .toList();
        } else if (search != null && !search.isBlank()) {
            users = userService.searchUsersByName(search);
        } else if (role != null && !role.isBlank()) {
            users = userService.getAllUsersWithProfile().stream()
                    .filter(u -> role.equalsIgnoreCase(u.getRole()))
                    .toList();
        } else {
            users = userService.getAllUsersWithProfile();
        }

        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("userList", users);

        return "page/admin-user-list";
    }

    @PostMapping("/admin/user/toggle-status/{userID}")
    public String toggleUserStatus(@PathVariable("userID") int userId,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "role", required = false) String role,
                                   HttpSession session) {
        userService.toggleUserStatus(userId);

        // Redirect back with search and role preserved
        String redirectUrl = "/admin-user-list";
        if ((search != null && !search.isBlank()) || (role != null && !role.isBlank())) {
            redirectUrl += "?";
            if (search != null && !search.isBlank()) {
                redirectUrl += "search=" + search;
            }
            if (role != null && !role.isBlank()) {
                redirectUrl += (search != null && !search.isBlank()) ? "&" : "";
                redirectUrl += "role=" + role;
            }
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/admin/user/change-role/{userID}")
    public String changeUserRole(@PathVariable("userID") int userId,
                                 @RequestParam("newRole") String newRole,
                                 @RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "role", required = false) String role,
                                 HttpSession session) {
        userService.updateUserRole(userId, newRole);
        String redirectUrl = "/admin-user-list";
        List<String> params = new ArrayList<>();

        if (role != null && !role.isBlank()) {
            params.add("role=" + role);
        }
        if (search != null && !search.isBlank()) {
            params.add("search=" + search);
        }
        if (!params.isEmpty()) {
            redirectUrl += "?" + String.join("&", params);
        }

        return "redirect:" + redirectUrl;
    }


}

