package org.swp391.hotelbookingsystem.controller.host;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.ReviewRepository;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.RoomTypeService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;


@Controller
public class HostDashboardController {

    final
    RoomTypeService roomTypeService;

    final
    LocationService locationService;

    final
    AmenityService amenityService;

    final
    CloudinaryService cloudinaryService;

    final
    RoomService roomService;
    final
    HotelService hotelService;

    final
    UserService userService;

    final
    BookingService bookingService;

    final
    ReviewService reviewService;

    public HostDashboardController(RoomTypeService roomTypeService, LocationService locationService, AmenityService amenityService, CloudinaryService cloudinaryService, RoomService roomService, HotelService hotelService, UserService userService, BookingService bookingService, ReviewService reviewService) {
        this.roomTypeService = roomTypeService;
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.cloudinaryService = cloudinaryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.reviewService = reviewService;
    }

    @GetMapping("/host-dashboard")
    public String showHostDashboard(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login"; // not logged in
        }

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);

        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);

        // Key Metrics
        model.addAttribute("totalHotels", hotelService.getHotelsByHostId(host.getId()).size());
        double monthlyRevenue = bookingService.getMonthlyRevenueByHostId(host.getId());
        model.addAttribute("monthlyRevenue", formatRevenue(monthlyRevenue));
        model.addAttribute("totalBookings", bookingService.countTotalBookingsByHostId(host.getId()));
        model.addAttribute("pendingBookings", bookingService.countPendingBookingsByHostId(host.getId()));

        ReviewRepository.RatingStats ratingStats = reviewService.getRatingStatsByHostId(host.getId()).orElse(new ReviewRepository.RatingStats(0.0, 0));
        model.addAttribute("averageRating", String.format("%.1f", ratingStats.getAverage()));
        model.addAttribute("totalReviews", ratingStats.getCount());

        // For recent bookings table
        model.addAttribute("bookings", bookingService.getBookingsByHostId(host.getId()));

        return "host/host-dashboard";
    }

    @GetMapping("/api/host/booking-stats")
    @ResponseBody
    public List<Map<String, Object>> getBookingStats(HttpSession session, @RequestParam String period) {
        User host = (User) session.getAttribute("user");
        if (host == null) {
            return List.of(); // or throw exception
        }
        return bookingService.getBookingStatsByHostId(host.getId(), period);
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
}
