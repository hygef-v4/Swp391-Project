package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;

import java.util.List;

@Controller
public class BookingHistoryController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/bookingHistory")
    public String bookingHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        int customerId = user.getId();

        List<Booking> upcomingBookings = bookingService.getUpcomingBookings(customerId);
        List<Booking> completedBookings = bookingService.getCompletedBookings(customerId);
        List<Booking> cancelledBookings = bookingService.getCancelledBookings(customerId);

        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);

        return "page/bookingHistory";
    }
}
