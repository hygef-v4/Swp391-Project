package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.service.BookingService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/admin-booking-list")
    public String showBookingList(@RequestParam(value = "keyword", required = false) String keyword,
                                  Model model) {
        List<Booking> bookings = (keyword != null && !keyword.isEmpty())
                ? bookingService.searchBookings(keyword)
                : bookingService.getAllBookings();

        model.addAttribute("bookings", bookings);

        // Example: list of tab filters
        model.addAttribute("tabs", List.of(
                Map.of("label", "All Status", "href", "#tab-1", "active", true),
                Map.of("label", "Available", "href", "#tab-2", "active", false),
                Map.of("label", "Sold Out", "href", "#tab-3", "active", false)
        ));

        // Example: quick stats
        model.addAttribute("bookingStats", List.of(
                statCard("New Booked Rooms", 54, "primary", "bi bi-door-open", 60, "16 new rooms booked"),
                statCard("Cancelled Rooms", 12, "danger", "bi bi-x-circle", 40, "2 canceled rooms"),
                statCard("Check-in", 20, "success", "bi bi-box-arrow-in-right", 45, "45 reservation"),
                statCard("Check-out", 28, "orange", "bi bi-box-arrow-right", 30, "30 reservation")
        ));

        return "page/admin-booking-list";
    }

    // Helper method to build stat card map
    private Map<String, Object> statCard(String label, int value, String color, String icon, int percent, String footer) {
        Map<String, Object> card = new HashMap<>();
        card.put("label", label);
        card.put("value", value);
        card.put("color", color);
        card.put("icon", icon);
        card.put("percent", percent);
        card.put("footerText", footer);
        return card;
    }
}
