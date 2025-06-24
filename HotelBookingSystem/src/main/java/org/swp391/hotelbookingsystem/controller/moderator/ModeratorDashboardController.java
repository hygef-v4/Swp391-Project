package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.HotelService;

@Controller
public class ModeratorDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/moderator-dashboard")
    public String getDashboard(Model model) {
        try {
            // Lấy tổng số người dùng
            int totalUsers = userService.getTotalUsers();
            model.addAttribute("totalUsers", totalUsers);

            // Lấy tổng số khách sạn
            int totalHotels = hotelService.getTotalHotels();
            model.addAttribute("totalHotels", totalHotels);

            // Lấy số lượng người dùng bị flagged
            long flaggedUsers = userService.getAllUsersWithProfile().stream().filter(u -> Boolean.TRUE.equals(u.isFlagged())).count();
            model.addAttribute("flaggedUsers", flaggedUsers);

            // Lấy tổng số người dùng hoạt động (không bị flagged)
            long activeUsers = userService.getAllUsersWithProfile().stream().filter(u -> u.isActive() && !Boolean.TRUE.equals(u.isFlagged())).count();
            model.addAttribute("activeUsers", activeUsers);

            // Lấy danh sách khách sạn chờ duyệt
            java.util.List<org.swp391.hotelbookingsystem.model.Hotel> pendingHotels = hotelService.getAllHotels().stream()
                .filter(h -> "pending".equals(h.getStatus()))
                .toList();
            model.addAttribute("pendingHotels", pendingHotels);
            model.addAttribute("pendingApprovals", pendingHotels.size());
        } catch (Exception e) {
            // Xử lý lỗi và set giá trị mặc định
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalHotels", 0);
            model.addAttribute("flaggedUsers", 0);
            model.addAttribute("activeUsers", 0);
            e.printStackTrace();
        }
        
        return "moderator/moderatorDashboard";
    }
}