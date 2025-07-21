package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpSession;
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
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminAgentController {
    @Value("${app.base-url}")
    private String baseUrl;
    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final ReportService reportService;
    private final NotificationService notificationService;

    public AdminAgentController(UserService userService, HotelService hotelService, BookingService bookingService, EmailService emailService, ReportService reportService, NotificationService notificationService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.bookingService = bookingService;
        this.emailService = emailService;
        this.reportService = reportService;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin-agent-list")
    public String showAgentList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "8") int size,
                                @RequestParam(defaultValue = "") String search,
                                @RequestParam(defaultValue = "") String sort,
                                Model model) {

        int offset = (page - 1) * size;

        List<User> agentList = userService.getAgentsSortedPaginated(search, sort, offset, size);
        int totalItems = userService.countAgentsBySearch(search);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        Map<Integer, Integer> hotelCountMap = new HashMap<>();
        Map<Integer, Double> revenueMap = new HashMap<>();
        Map<Integer, Double> avgRatingMap = new HashMap<>();
        for (User agent : agentList) {
            hotelCountMap.put(agent.getId(), hotelService.countHotelsByHostId(agent.getId()));
            revenueMap.put(agent.getId(), bookingService.getTotalRevenueByHostId(agent.getId()));
            double avgRating = hotelService.getAverageRatingByHotelId(agent.getId());
            avgRatingMap.put(agent.getId(), avgRating);
        }

        model.addAttribute("avgRatingMap", avgRatingMap);
        model.addAttribute("totalAgent", userService.countAgent());
        model.addAttribute("agentList", agentList);
        model.addAttribute("hotelCountMap", hotelCountMap);
        model.addAttribute("revenueMap", revenueMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "admin/admin-agent-list";
    }

    @GetMapping("/admin-agent-detail")
    public String showAgentDetail(@RequestParam("id") int agentId, Model model) {
        User agent = userService.findUserById(agentId);

        List<Hotel> hotelList = hotelService.getHotelsByHostId(agent.getId());
        int countHotel = hotelList.size();
        int totalBooking = bookingService.countBookingsByHostId(agent.getId());
        double monthlyRevenue = bookingService.getMonthlyRevenueByHostId(agent.getId());
        double avgRating = hotelService.getAverageRatingByHotelId(agent.getId());
        String revenue = formatRevenue(monthlyRevenue);

        model.addAttribute("avgRating", avgRating);
        model.addAttribute("monthlyRevenue", revenue);
        model.addAttribute("totalBooking", totalBooking);
        model.addAttribute("countHotel", countHotel);
        model.addAttribute("hotelList", hotelList);
        model.addAttribute("agent", agent);
        return "admin/admin-agent-detail";
    }

    @PostMapping("/admin-agent/ban/{userId}")
    public String banAgent(@PathVariable("userId") int userId,
                           @RequestParam("reason") String reason,
                           @RequestParam(value = "page", required = false) Integer page,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "sort", required = false) String sort,
                           HttpSession session) throws MessagingException {

        User user = userService.findUserById(userId);
        if (user == null) {
            String errorMsg = "Không tìm thấy người dùng.";
            String redirectUrl = buildRedirectUrl("/admin-agent-list", search, null, null, page, sort);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        // Get current admin user from session
        User currentAdmin = (User) session.getAttribute("user");
        if (currentAdmin != null && currentAdmin.getId() == userId) {
            String errorMsg = "Không thể khóa tài khoản của chính mình.";
            String redirectUrl = buildRedirectUrl("/admin-agent-list", search, null, null, page, sort);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        if (reason == null || reason.trim().isEmpty()) {
            String errorMsg = "Lý do huỷ đối tác không được để trống.";
            String redirectUrl = buildRedirectUrl("/admin-agent-list", search, null, null, page, sort);
            String separator = redirectUrl.contains("?") ? "&" : "?";
            return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
        }

        if (user != null && user.isActive()) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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
                        String redirectUrl = buildRedirectUrl("/admin-agent-list", search, null, null, page, sort);
                        String separator = redirectUrl.contains("?") ? "&" : "?";
                        return "redirect:" + redirectUrl + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
                    }
                }
            }

            userService.banUser(userId); // đặt is_active = false
            emailService.sendUserBanEmail(user.getEmail(), reason);
            hotelService.banAllHotelsByHostId(userId); // huỷ toàn bộ khách sạn
            if (reportService.isUserFlagged(userId)) {
                reportService.acceptUserReports(userId);
            }
        }

        return "redirect:" + buildRedirectUrl("/admin-agent-list", search, null, null, page, sort);
    }

    private String buildRedirectUrl(String baseUrl, String search, String role, String status, Integer page, String sort) {
        StringBuilder url = new StringBuilder(baseUrl);
        boolean first = true;

        if (search != null && !search.isEmpty()) {
            url.append(first ? "?" : "&").append("search=").append(search);
            first = false;
        }
        if (role != null && !role.isEmpty()) {
            url.append(first ? "?" : "&").append("role=").append(role);
            first = false;
        }
        if (status != null && !status.isEmpty()) {
            url.append(first ? "?" : "&").append("status=").append(status);
            first = false;
        }
        if (page != null) {
            url.append(first ? "?" : "&").append("page=").append(page);
            first = false;
        }
        if (sort != null && !sort.isEmpty()) {
            url.append(first ? "?" : "&").append("sort=").append(sort);
        }

        return url.toString();
    }


    private String formatRevenue(double revenue) {
        if (revenue >= 1_000_000_000) {
            return new DecimalFormat("#,##0.0 'Tỷ'").format(revenue / 1_000_000_000);
        }
        if (revenue >= 1_000_000) {
            return new DecimalFormat("#,##0.0 'Tr'").format(revenue / 1_000_000);
        }
        if (revenue >= 1_000) {
            return new DecimalFormat("#,##0 'K'").format(revenue / 1_000);
        }
        return new DecimalFormat("#,##0").format(revenue);
    }

    // Note: Chat functionality has been moved to ChatPageController
    // These methods are kept for backward compatibility but should be deprecated

}
