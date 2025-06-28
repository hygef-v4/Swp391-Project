package org.swp391.hotelbookingsystem.controller.shared;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.service.ReviewService;

import java.util.List;

@Controller
public class HomeController {
    final LocationService locationService;

    final HotelService hotelService;

    final ReviewService reviewService;

    public HomeController(LocationService locationService, HotelService hotelService, ReviewService reviewService) {
        this.locationService = locationService;
        this.hotelService = hotelService;
        this.reviewService = reviewService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking");
        List<Location> locations = locationService.getAllLocations();
        session.setAttribute(ConstantVariables.LOCATIONS, locations);

        // Fetch top 4 high-rated hotels and add to model
        List<Hotel> topHotels = hotelService.getTop8HighRatedHotels();
        model.addAttribute("topHotels", topHotels);

        // Fetch top 5 public positive reviews and add to model
        List<Review> top5Reviews = reviewService.getTop5PublicPositiveReviews();
        model.addAttribute("top5Reviews", top5Reviews);


        return "page/homepage";
    }
}
