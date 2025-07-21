package org.swp391.hotelbookingsystem.controller.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Report;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.ReportService;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminUserController {
    @Value("${app.base-url}")
    private String baseUrl;
    private final UserService userService;
    private final BookingService bookingService;
    private final HotelService hotelService;
    private final ReportService reportService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public AdminUserController(UserService userService, BookingService bookingService, 
                             HotelService hotelService, ReportService reportService, EmailService emailService, NotificationService notificationService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.hotelService = hotelService;
        this.reportService = reportService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin-user-list")
    public String getUserList(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "error", required = false) String error,
                              Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        // Add error message to model if present
        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
        }

        String trimmedSearch = (search != null) ? search.trim().replaceAll("\\s+", " ") : null;
        String trimmedRole = (role != null) ? role.trim() : null;
        String trimmedStatus = (status != null) ? status.trim() : null;

        List<User> filteredUsers;

        if ("banned".equals(trimmedStatus)) {
            filteredUsers = userService.getAllUsersWithProfile().stream()
                    .filter(u -> !u.isActive())
                    .toList();
        } else if ("flagged".equals(trimmedStatus)) {
            filteredUsers = userService.getAllUsersWithProfile().stream()
                    .filter(u -> userService.isUserFlagged(u.getId()))
                    .toList();
        } else if (trimmedSearch != null && !trimmedSearch.isBlank() && trimmedRole != null && !trimmedRole.isBlank()) {
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

        // Set flagged property for each user in the filtered list
        for (User u : filteredUsers) {
            u.setFlagged(userService.isUserFlagged(u.getId()));
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
        model.addAttribute("status", trimmedStatus);
        model.addAttribute("userList", currentPageUsers);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalUsers / pageSize));

        if (error != null && !error.isBlank()) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/admin-user-list";
    }

    @PostMapping("/admin/user/unflag/{userID}")
    public String unflagUser(@PathVariable("userID") int userId,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "role", required = false) String role,
                             @RequestParam(value = "status", required = false) String status) {
        reportService.unflagUser(userId); // sets reports to 'declined'
        return "redirect:" + buildRedirectUrl("/admin-user-list", search, role, status, null);
    }

    @PostMapping("/admin/user/toggle-status/{userID}")
    public String toggleUserStatus(@PathVariable("userID") int userId,
                                   @RequestParam(value = "reason", required = false) String reason,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "role", required = false) String role,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "page", required = false) Integer page,
                                   HttpSession session) {
        User user = userService.findUserById(userId);
        if (user == null) {
            String errorMsg = "Không tìm thấy người dùng.";
            String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, page);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        // Get current admin user from session
        User currentAdmin = (User) session.getAttribute("user");
        if (currentAdmin != null && currentAdmin.getId() == userId) {
            String errorMsg = "Không thể thay đổi trạng thái của chính mình.";
            String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, page);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        boolean wasActive = user.isActive();

        // If banning, require a non-blank reason BEFORE toggling status
        if (wasActive) {
            if (reason == null || reason.trim().isEmpty()) {
                String errorMsg = "Lý do khóa tài khoản không được để trống hoặc chỉ chứa khoảng trắng.";
                String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, page);
                String separator = redirectUrl.contains("?") ? "&" : "?";
                return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        List<Booking> bookings = bookingService.findActiveBookingsByCustomerId(userId);

        for(Booking booking : bookings){
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", String.valueOf(booking.getBookingId()));
            params.add("trantype", "02");
            params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
            params.add("refundRole", "Hotel Owner");
            params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            String response = restTemplate.postForObject(baseUrl + "/refund", request, String.class);

            if(response != null && response.equals("00")){
                notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), booking.refundAmount());
            }else{
                String errorMsg = "Hoàn tiền thất bại";
                String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, page);
                String separator = redirectUrl.contains("?") ? "&" : "?";
                return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        userService.toggleUserStatus(userId);
        // Refresh user object after status change
        User updatedUser = userService.findUserById(userId);
        try {
            if (wasActive && !updatedUser.isActive()) {
                // Ban: send ban email
                emailService.sendUserBanEmail(updatedUser.getEmail(), reason);
                // Accept reports if flagged
                if (reportService.isUserFlagged(userId)) {
                    reportService.acceptUserReports(userId);
                }
                // Ban all hotels if user is a hotel owner
                if ("HOTEL_OWNER".equals(updatedUser.getRole())) {
                    List<Booking> hostBookings = bookingService.findActiveBookingsByHostId(userId);

                    for(Booking booking : hostBookings){
                        if(booking.getCustomerId() != booking.getHostId()){
                            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                            params.add("id", String.valueOf(booking.getBookingId()));
                            params.add("trantype", "02");
                            params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
                            params.add("refundRole", "Hotel Owner");
                            params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

                            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
                            String response = restTemplate.postForObject(baseUrl + "/refund", request, String.class);

                            if(response != null && response.equals("00")){
                                notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), booking.refundAmount());
                            }else{
                                String errorMsg = "Hoàn tiền thất bại";
                                String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, page);
                                String separator = redirectUrl.contains("?") ? "&" : "?";
                                return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
                            }
                        }
                    }

                    hotelService.banAllHotelsByHostId(userId);
                }
            } else if (!wasActive && updatedUser.isActive()) {
                // Unban: send unban email (reason optional)
                emailService.sendUserUnbanEmail(updatedUser.getEmail(), reason != null ? reason : "");
            }
        } catch (Exception e) {
            // log error
        }
        return "redirect:" + buildRedirectUrl("/admin-user-list", search, role, status, page);
    }

    @PostMapping("/admin/user/change-role/{userID}")
    public String changeUserRole(@PathVariable("userID") int userId,
                                 @RequestParam("newRole") String newRole,
                                 @RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "role", required = false) String role,
                                 @RequestParam(value = "status", required = false) String status,
                                 HttpSession session) {

        // Get current admin user from session
        User currentAdmin = (User) session.getAttribute("user");
        if (currentAdmin != null && currentAdmin.getId() == userId) {
            String errorMsg = "Không thể thay đổi vai trò của chính mình.";
            String redirectUrl = buildRedirectUrl("/admin-user-list", search, role, status, null);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        userService.updateUserRole(userId, newRole);

        return "redirect:" + buildRedirectUrl("/admin-user-list", search, role, status, null);
    }

    private String buildRedirectUrl(String base, String search, String role, String status, Integer page) {
        List<String> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) params.add("search=" + search.trim());
        if (role != null && !role.trim().isEmpty()) params.add("role=" + role.trim());
        if (status != null && !status.trim().isEmpty()) params.add("status=" + status.trim());
        if (page != null && page > 1) params.add("page=" + page);
        return base + (params.isEmpty() ? "" : "?" + String.join("&", params));
    }

    @GetMapping("/admin-user-detail")
    public String showUserDetail(@RequestParam("id") int userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        // Get user details
        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/admin-user-list?error=user-not-found";
        }

        // Translate gender to Vietnamese
        String genderDisplay = "Chưa cập nhật";
        if (user.getGender() != null) {
            switch (user.getGender().toLowerCase()) {
                case "male":
                    genderDisplay = "Nam";
                    break;
                case "female":
                    genderDisplay = "Nữ";
                    break;
                case "other":
                    genderDisplay = "Giới tính khác";
                    break;
                default:
                    genderDisplay = "Giới tính khác";
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("genderDisplay", genderDisplay);

        // Role-specific data
        if ("HOTEL_OWNER".equals(user.getRole())) {
            // Get hotels owned by this user
            List<Hotel> hotels = hotelService.getHotelsByHostId(userId);
            model.addAttribute("hotels", hotels);
            model.addAttribute("hotelCount", hotels.size());

            // Get booking statistics for hotel owner
            double totalRevenue = bookingService.getTotalRevenueByHostId(userId);
            int totalBookings = bookingService.countTotalBookingsByHostId(userId);
            double monthlyRevenue = bookingService.getMonthlyRevenueByHostId(userId);
            
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("monthlyRevenue", monthlyRevenue);

            // Get recent bookings for this host - follow host dashboard logic
            List<Booking> allHostBookings = bookingService.getBookingsByHostId(userId);
            
            // Filter out bookings with only pending booking units and calculate status (same as host dashboard)
            List<Booking> recentBookings = allHostBookings.stream()
                    .filter(booking -> {
                        if (booking.getBookingUnits() == null || booking.getBookingUnits().isEmpty()) {
                            return false;
                        }
                        
                        // Check if booking has at least one non-pending booking unit
                        boolean hasNonPendingUnit = booking.getBookingUnits().stream()
                                .anyMatch(unit -> {
                                    String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                    return !"pending".equals(unitStatus);
                                });
                        
                        if (hasNonPendingUnit) {
                            // Filter out pending booking units for this booking
                            List<BookingUnit> nonPendingUnits = booking.getBookingUnits().stream()
                                    .filter(unit -> {
                                        String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                        return !"pending".equals(unitStatus);
                                    })
                                    .collect(java.util.stream.Collectors.toList());
                            
                            // Set the filtered booking units
                            booking.setBookingUnits(nonPendingUnits);
                            
                            // Calculate number of nights (same as host dashboard)
                            booking.calculateNumberOfNights();

                            // Determine booking status (same as host dashboard)
                            String overallStatus = booking.determineStatus();
                            booking.setStatus(overallStatus);

                            // Calculate total price using same method as host dashboard
                            if ("cancelled".equals(overallStatus) || "rejected".equals(overallStatus)) {
                                booking.setTotalPrice(0.0);
                            } else {
                                // Use calculateTotalPrice() method which properly calculates: price * quantity * numberOfNights
                                double calculatedPrice = booking.calculateTotalPrice();
                                booking.setTotalPrice(calculatedPrice);
                            }
                            
                            return true;
                        }
                        
                        return false; // Hide bookings with only pending units
                    })
                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("recentBookings", recentBookings);
        }

        if ("CUSTOMER".equals(user.getRole())) {
            // Get all bookings to filter for this customer
            List<Booking> allBookings = bookingService.getAllBookings();
            
            // Filter customer bookings and exclude those with only pending booking units
            List<Booking> customerBookings = allBookings.stream()
                    .filter(booking -> booking.getCustomerId() == userId)
                    .filter(booking -> {
                        if (booking.getBookingUnits() == null || booking.getBookingUnits().isEmpty()) {
                            return false;
                        }
                        // Check if booking has at least one non-pending booking unit
                        return booking.getBookingUnits().stream()
                                .anyMatch(unit -> {
                                    String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                    return !"pending".equals(unitStatus);
                                });
                    })
                    .map(booking -> {
                        // Filter out pending booking units and recalculate status
                        List<BookingUnit> nonPendingUnits = booking.getBookingUnits().stream()
                                .filter(unit -> {
                                    String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                    return !"pending".equals(unitStatus);
                                })
                                .toList();
                        
                        booking.setBookingUnits(nonPendingUnits);

                        // Calculate number of nights (same as host dashboard)
                        booking.calculateNumberOfNights();

                        // Determine booking status (same as host dashboard)
                        String overallStatus = booking.determineStatus();
                        booking.setStatus(overallStatus);

                        // Calculate total price using same method as host dashboard
                        if ("cancelled".equals(overallStatus) || "rejected".equals(overallStatus)) {
                            booking.setTotalPrice(0.0);
                        } else {
                            // Use calculateTotalPrice() method which properly calculates: price * quantity * numberOfNights
                            double calculatedPrice = booking.calculateTotalPrice();
                            booking.setTotalPrice(calculatedPrice);
                        }
                        
                        return booking;
                    })
                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                    .limit(10)
                    .toList();

            // Calculate customer statistics - follow same logic as host revenue calculation
            long totalBookings = customerBookings.size();
            
            // Calculate total spent - sum of booking total prices (already calculated above)
            double totalSpent = customerBookings.stream()
                    .mapToDouble(booking -> booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0)
                    .sum();

            model.addAttribute("customerBookings", customerBookings);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalSpent", totalSpent);
        }

        // Check if user is flagged
        boolean isFlagged = reportService.isUserFlagged(userId);
        model.addAttribute("isFlagged", isFlagged);

        if (isFlagged) {
            String formattedReasons = reportService.getFormattedUserReportReasons(userId);
            model.addAttribute("flagReason", formattedReasons);
        }

        return "admin/admin-user-detail";
    }

}

