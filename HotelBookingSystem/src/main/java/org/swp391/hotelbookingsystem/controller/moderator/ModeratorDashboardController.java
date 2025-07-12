package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.model.Report;
import org.swp391.hotelbookingsystem.service.ReportService;

@Controller
public class ModeratorDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/moderator-dashboard")
    public String getDashboard(Model model) {
        try {
            // Lấy tổng số người dùng
            int totalUsers = userService.getTotalUsers();
            model.addAttribute("totalUsers", totalUsers);

            // Lấy tổng số khách sạn
            int totalHotels = hotelService.getTotalHotels();
            model.addAttribute("totalHotels", totalHotels);

            // Lấy danh sách report pending
            java.util.List<Report> pendingReports = reportService.getAllPendingReports();
            java.util.Set<Integer> flaggedUserIds = new java.util.HashSet<>();
            java.util.Map<Integer, String> flagReasons = new java.util.HashMap<>();
            for (Report r : pendingReports) {
                flaggedUserIds.add(r.getReportedUserId());
                flagReasons.put(r.getReportedUserId(), r.getReason());
            }
            java.util.List<org.swp391.hotelbookingsystem.model.User> allUsers = userService.getAllUsersWithProfile();
            java.util.List<org.swp391.hotelbookingsystem.model.User> flaggedUsersList = new java.util.ArrayList<>();
            for (org.swp391.hotelbookingsystem.model.User u : allUsers) {
                if (flaggedUserIds.contains(u.getId())) {
                    u.setFlagged(true);
                    u.setFlagReason(flagReasons.get(u.getId()));
                    flaggedUsersList.add(u);
                }
            }
            model.addAttribute("flaggedUsers", flaggedUsersList.size());
            model.addAttribute("flaggedUsersList", flaggedUsersList);
            model.addAttribute("flaggedUsersListLimited", flaggedUsersList.size() > 4 ? flaggedUsersList.subList(0, 4) : flaggedUsersList);

            // Lấy tổng số người dùng hoạt động (không bị flagged)
            long activeUsers = allUsers.stream().filter(u -> u.isActive() && !flaggedUserIds.contains(u.getId())).count();
            model.addAttribute("activeUsers", activeUsers);

            // Lấy danh sách khách sạn chờ duyệt
            java.util.List<org.swp391.hotelbookingsystem.model.Hotel> pendingHotels = hotelService.getAllHotels().stream()
                .filter(h -> "pending".equals(h.getStatus()))
                .toList();
            model.addAttribute("pendingHotels", pendingHotels);
            model.addAttribute("pendingApprovals", pendingHotels.size());

            // Lấy thông tin chủ khách sạn cho từng khách sạn chờ duyệt
            java.util.Map<Integer, org.swp391.hotelbookingsystem.model.User> hostMap = new java.util.HashMap<>();
            for (org.swp391.hotelbookingsystem.model.Hotel hotel : pendingHotels) {
                if (!hostMap.containsKey(hotel.getHostId())) {
                    org.swp391.hotelbookingsystem.model.User host = userService.findUserById(hotel.getHostId());
                    if (host != null) hostMap.put(hotel.getHostId(), host);
                }
            }
            model.addAttribute("hostMap", hostMap);
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