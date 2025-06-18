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



        return "admin/admin-dashboard";
    }
}
