package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;

import java.util.List;

@Controller
public class ModeratorUserListController {

    @Autowired
    private UserService userService;

    private static final int PAGE_SIZE = 8;

    @GetMapping("/moderator-user-list")
    public String getUserList(Model model) {
        List<User> userList = userService.getAllUsersWithProfile();
        long activeUsers = userList.stream().filter(User::isActive).count();
        long lockedUsers = userList.size() - activeUsers;

        model.addAttribute("users", userList);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("lockedUsers", lockedUsers);
        model.addAttribute("reportedUsers", 0); // Placeholder for reported users
        return "moderator/moderatorUserList";
    }

    @PostMapping("/moderator-user-list/toggle-status/{userId}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        try {
            userService.toggleUserStatus(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error toggling user status");
        }
    }
}

