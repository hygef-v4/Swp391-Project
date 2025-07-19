package org.swp391.hotelbookingsystem.controller.shared;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    final LocationService locationService;

    final HotelService hotelService;

    final ReviewService reviewService;

    final UserService userService;

    public HomeController(LocationService locationService, HotelService hotelService, ReviewService reviewService, UserService userService) {
        this.locationService = locationService;
        this.hotelService = hotelService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "Hamora Booking");
        List<Location> locations = locationService.getAllLocations();
        session.setAttribute("locations", locations);

        // Fetch top 4 high-rated hotels and add to model
        List<Hotel> topHotels = hotelService.getTop8HighRatedHotels();
        model.addAttribute("topHotels", topHotels);

        // Fetch top 5 public positive reviews and add to model
        List<Review> top5Reviews = reviewService.getTop5PublicPositiveReviews();
        model.addAttribute("top5Reviews", top5Reviews);

        // Add customer statistics for the hero section
        List<User> customers = userService.getUsersByRole("CUSTOMER");
        model.addAttribute("customerCount", customers.size());

        // Get top 5 customers with avatars for display
        List<User> topCustomers = customers.size() > 5 ? customers.subList(0, 5) : customers;
        model.addAttribute("topCustomers", topCustomers);

        // Add overall average rating
        double overallRating = reviewService.getOverallAverageRating();
        model.addAttribute("overallRating", String.format("%.1f", overallRating));

        return "page/homepage";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Thông báo của tôi");
        return "page/notifications";
    }
}
