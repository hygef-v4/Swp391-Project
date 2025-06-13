package org.swp391.hotelbookingsystem.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.repository.BookingRepo;
import org.swp391.hotelbookingsystem.service.*;

import java.util.*;

@Controller
public class AdminDashboardController {

    final
    BookingService bookingService;

    final
    HotelService hotelService;

    final
    RoomService roomService;

    final
    ReviewService reviewService;

    final
    UserService userService;

    final
    LocationService locationService;

    public AdminDashboardController(BookingService bookingService, HotelService hotelService, RoomService roomService, ReviewService reviewService, UserService userService, LocationService locationService) {
        this.bookingService = bookingService;
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.locationService = locationService;
    }

    @GetMapping("/admin-dashboard")
    public String getDashboard(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking");

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        int numberOfHotels = hotelService.countHotels();
        model.addAttribute("numberOfHotels", numberOfHotels);

        int totalRooms = roomService.countRooms();
        model.addAttribute("totalRooms", totalRooms);

        List<User> customerList = userService.getUsersByRole("CUSTOMER");
        model.addAttribute("numberOfCustomers", customerList.size());

        List<User> hotelOwnerList = userService.getUsersByRole("HOTEL_OWNER");
        model.addAttribute("numberOfHotelOwners", hotelOwnerList.size());

        List<User> getTop5UsersWithProfile = userService.getTop5Users();
        model.addAttribute("userList", getTop5UsersWithProfile);

        List<Hotel> popularHotels = hotelService.getTop4PopularHotels();
        model.addAttribute("popularHotels", popularHotels);

        List<Review> recentReviews = reviewService.getRecentPublicReviews();
        model.addAttribute("recentReviews", recentReviews);

        List<Location> getTop5Location = locationService.getTop5Locations();
        model.addAttribute("locationList", getTop5Location);

        // Merge dates from both stats
        Set<String> allDates = new TreeSet<>();
        Map<String, Integer> checkInMap = new HashMap<>();
        Map<String, Integer> checkOutMap = new HashMap<>();

        for (BookingRepo.DailyStat stat : bookingService.getCheckInStats()) {
            allDates.add(stat.date());
            checkInMap.put(stat.date(), stat.count());
        }
        for (BookingRepo.DailyStat stat : bookingService.getCheckOutStats()) {
            allDates.add(stat.date());
            checkOutMap.put(stat.date(), stat.count());
        }

// Prepare aligned chart data
        List<String> dateLabels = new ArrayList<>(allDates);
        List<Integer> checkInData = new ArrayList<>();
        List<Integer> checkOutData = new ArrayList<>();

        for (String date : dateLabels) {
            checkInData.add(checkInMap.getOrDefault(date, 0));
            checkOutData.add(checkOutMap.getOrDefault(date, 0));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            model.addAttribute("dateLabels", objectMapper.writeValueAsString(dateLabels));
            model.addAttribute("checkInData", objectMapper.writeValueAsString(checkInData));
            model.addAttribute("checkOutData", objectMapper.writeValueAsString(checkOutData));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int checkInCount = checkInData.stream().mapToInt(Integer::intValue).sum();
        int checkOutCount = checkOutData.stream().mapToInt(Integer::intValue).sum();

        model.addAttribute("checkInCount", checkInCount);
        model.addAttribute("checkOutCount", checkOutCount);

        return "admin/admin-dashboard";
    }
}
