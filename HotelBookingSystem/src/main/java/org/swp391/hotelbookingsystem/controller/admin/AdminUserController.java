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

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin-user-list")
    public String getUserList(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        String trimmedSearch = (search != null) ? search.trim().replaceAll("\\s+", " ") : null;
        String trimmedRole = (role != null) ? role.trim() : null;

        List<User> filteredUsers;

        if (trimmedSearch != null && !trimmedSearch.isBlank() && trimmedRole != null && !trimmedRole.isBlank()) {
            filteredUsers = userService.getAllUsersWithProfile().stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(trimmedSearch.toLowerCase()))
                    .filter(u -> trimmedRole.equalsIgnoreCase(u.getRole()))
                    .toList();
        } else if (trimmedSearch != null && !trimmedSearch.isBlank()) {
            filteredUsers = userService.searchUsersByName(search);
        } else if (trimmedRole != null && !trimmedRole.isBlank()) {
            filteredUsers = userService.getAllUsersWithProfile().stream()
                    .filter(u -> trimmedRole.equalsIgnoreCase(u.getRole()))
                    .toList();
        } else {
            filteredUsers = userService.getAllUsersWithProfile();
        }


        int pageSize = 10;
        int totalUsers = filteredUsers.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalUsers);

        List<User> currentPageUsers = (startIndex < totalUsers) ? filteredUsers.subList(startIndex, endIndex) : List.of();

        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("search", trimmedSearch);
        model.addAttribute("role", trimmedRole);
        model.addAttribute("userList", currentPageUsers);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalUsers / pageSize));

        return "admin/admin-user-list";
    }

    @PostMapping("/admin/user/toggle-status/{userID}")
    public String toggleUserStatus(@PathVariable("userID") int userId,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "role", required = false) String role) {
        userService.toggleUserStatus(userId);

        return "redirect:" + buildRedirectUrl("/admin-user-list", search, role);
    }

    @PostMapping("/admin/user/change-role/{userID}")
    public String changeUserRole(@PathVariable("userID") int userId,
                                 @RequestParam("newRole") String newRole,
                                 @RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "role", required = false) String role) {
        userService.updateUserRole(userId, newRole);

        return "redirect:" + buildRedirectUrl("/admin-user-list", search, role);
    }

    private String buildRedirectUrl(String base, String search, String role) {
        search = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        role = (role != null && !role.trim().isEmpty()) ? role.trim() : null;

        List<String> params = new ArrayList<>();
        if (search != null) params.add("search=" + search);
        if (role != null) params.add("role=" + role);

        return base + (params.isEmpty() ? "" : "?" + String.join("&", params));
    }


}

