package org.swp391.hotelbookingsystem.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ChatPageController {

    private final UserService userService;

    public ChatPageController(UserService userService) {
        this.userService = userService;
    }

    // Hotel Owner to Admin Chat
    @GetMapping("/agent-admin-contact")
    public String agentAdminContact(@RequestParam("userId") int userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is hotel owner
        if (!"HOTEL_OWNER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ chủ khách sạn mới có thể liên hệ admin.");
        }

        // Get the specific admin by userId instead of random selection
        User admin = userService.findUserById(userId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new IllegalStateException("Không tìm thấy admin được chỉ định.");
        }

        model.addAttribute("customer", admin);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "agent-admin");
        return "admin/admin-agent-contact";
    }

    // Admin to Hotel Owner Chat
    @GetMapping("/admin-agent-contact")
    public String adminAgentContact(@RequestParam("userId") int agentId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        // Validate that current user is admin
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể liên hệ chủ khách sạn.");
        }
        
        User agent = userService.findUserById(agentId);
        if (agent == null || !"HOTEL_OWNER".equals(agent.getRole())) {
            throw new IllegalStateException("Không tìm thấy chủ khách sạn.");
        }
        
        model.addAttribute("customer", agent);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "admin-agent");
        return "admin/admin-agent-contact";
    }

    // Customer to Moderator Chat
    @GetMapping("/customer-moderator-contact")
    public String customerModeratorContact(@RequestParam("userId") int userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is customer
        if (!"CUSTOMER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ khách hàng mới có thể liên hệ moderator.");
        }

        // Get the specific moderator by userId instead of random selection
        User moderator = userService.findUserById(userId);
        if (moderator == null || !"MODERATOR".equals(moderator.getRole())) {
            throw new IllegalStateException("Không tìm thấy moderator được chỉ định.");
        }

        model.addAttribute("customer", moderator);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "customer-moderator");
        return "admin/admin-agent-contact";
    }

    // Moderator to Customer Chat
    @GetMapping("/moderator-customer-contact")
    public String moderatorCustomerContact(@RequestParam("userId") int customerId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        // Validate that current user is moderator
        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ moderator mới có thể liên hệ khách hàng.");
        }
        
        User customer = userService.findUserById(customerId);
        if (customer == null || !"CUSTOMER".equals(customer.getRole())) {
            throw new IllegalStateException("Không tìm thấy khách hàng.");
        }
        
        model.addAttribute("customer", customer);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "moderator-customer");
        return "admin/admin-agent-contact";
    }

    // Admin to Customer Chat
    @GetMapping("/admin-customer-contact")
    public String adminCustomerContact(@RequestParam("userId") int customerId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        // Validate that current user is admin
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể liên hệ khách hàng.");
        }
        
        User customer = userService.findUserById(customerId);
        if (customer == null || !"CUSTOMER".equals(customer.getRole())) {
            throw new IllegalStateException("Không tìm thấy khách hàng.");
        }
        
        model.addAttribute("customer", customer);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "admin-customer");
        return "admin/admin-agent-contact";
    }

    // Customer to Admin Chat
    @GetMapping("/customer-admin-contact")
    public String customerAdminContact(@RequestParam("userId") int userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is customer
        if (!"CUSTOMER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ khách hàng mới có thể liên hệ admin.");
        }

        // Get the specific admin by userId instead of random selection
        User admin = userService.findUserById(userId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new IllegalStateException("Không tìm thấy admin được chỉ định.");
        }

        model.addAttribute("customer", admin);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "customer-admin");
        return "admin/admin-agent-contact";
    }

    // Hotel Owner to Moderator Chat
    @GetMapping("/agent-moderator-contact")
    public String agentModeratorContact(@RequestParam("userId") int userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is hotel owner
        if (!"HOTEL_OWNER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ chủ khách sạn mới có thể liên hệ kiểm duyệt viên.");
        }

        User moderator = userService.findUserById(userId);
        if (moderator == null || !"MODERATOR".equals(moderator.getRole())) {
            throw new IllegalStateException("Không tìm thấy kiểm duyệt viên.");
        }

        model.addAttribute("customer", moderator);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "agent-moderator");
        return "admin/admin-agent-contact";
    }

    // Moderator to Hotel Owner Chat
    @GetMapping("/moderator-agent-contact")
    public String moderatorAgentContact(@RequestParam("userId") int agentId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is moderator
        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ kiểm duyệt viên mới có thể liên hệ chủ khách sạn.");
        }

        User agent = userService.findUserById(agentId);
        if (agent == null || !"HOTEL_OWNER".equals(agent.getRole())) {
            throw new IllegalStateException("Không tìm thấy chủ khách sạn.");
        }

        model.addAttribute("customer", agent);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "moderator-agent");
        return "admin/admin-agent-contact";
    }

    // Moderator to Admin Chat
    @GetMapping("/moderator-admin-contact")
    public String moderatorAdminContact(@RequestParam("userId") int adminId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is moderator
        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ kiểm duyệt viên mới có thể liên hệ admin.");
        }

        User admin = userService.findUserById(adminId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new IllegalStateException("Không tìm thấy admin.");
        }

        model.addAttribute("customer", admin);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "moderator-admin");
        return "admin/admin-agent-contact";
    }

    // Admin to Moderator Chat
    @GetMapping("/admin-moderator-contact")
    public String adminModeratorContact(@RequestParam("userId") int moderatorId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Validate that current user is admin
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể liên hệ kiểm duyệt viên.");
        }

        User moderator = userService.findUserById(moderatorId);
        if (moderator == null || !"MODERATOR".equals(moderator.getRole())) {
            throw new IllegalStateException("Không tìm thấy kiểm duyệt viên.");
        }

        model.addAttribute("customer", moderator);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatType", "admin-moderator");
        return "admin/admin-agent-contact";
    }
}
