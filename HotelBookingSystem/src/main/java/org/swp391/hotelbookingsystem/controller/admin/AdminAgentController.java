package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class AdminAgentController {

    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;

    public AdminAgentController(UserService userService, HotelService hotelService, BookingService bookingService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }


    @GetMapping("/admin-agent-list")
    public String showAgentList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "8") int size,
                                @RequestParam(defaultValue = "") String search,
                                @RequestParam(defaultValue = "") String sort,
                                Model model) {

        int offset = (page - 1) * size;

        List<User> agentList = userService.getAgentsSortedPaginated(search, sort, offset, size); // new method
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

        model.addAttribute("avgRating", avgRating);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("totalBooking", totalBooking);
        model.addAttribute("countHotel", countHotel);
        model.addAttribute("hotelList", hotelList);
        model.addAttribute("agent", agent);
        return "admin/admin-agent-detail";
    }

    @GetMapping("/admin-agent-contact")
    public String showAgentContact(@RequestParam("id") int agentId, Model model,
                                   HttpSession session) {
        User host = (User) session.getAttribute("user");
        User agent = userService.findUserById(agentId);
        model.addAttribute("customer", agent);
        model.addAttribute("currentUserId", host.getId());
        return "admin/admin-agent-contact";
    }

    @GetMapping("/agent-admin-contact")
    public String showAdminContact(@RequestParam("id") int agentId, Model model,
                                   HttpSession session) {
        User host = (User) session.getAttribute("user");
        List<User> admins = userService.getUsersByRole("ADMIN");

        if (admins.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy admin nào để liên hệ.");
        }

        User randomAdmin = admins.get(new Random().nextInt(admins.size()));

        model.addAttribute("customer", randomAdmin);
        model.addAttribute("currentUserId", host.getId());
        return "admin/admin-agent-contact";
    }

}
