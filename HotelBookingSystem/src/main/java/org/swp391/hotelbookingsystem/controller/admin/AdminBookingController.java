package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.service.BookingService;

import java.util.*;

@Controller
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/admin-booking-list")
    public String showBookingList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "grid") String view,
                                  @RequestParam(required = false) String search,
                                  Model model) {

        List<Booking> bookings = bookingService.getBookingsByStatusAndSearchPaginated(status, search, page - 1, size);
        int totalPages = bookingService.getTotalPagesByStatusAndSearch(status, search, size);

        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("statusFilter", status);
        model.addAttribute("viewMode", view);
        model.addAttribute("search", search);

        // Statistics
        model.addAttribute("totalBooked", bookingService.getTotalBooking("approved"));
        model.addAttribute("todayBooked", bookingService.getTodayBooking("approved"));
        model.addAttribute("totalCancelled", bookingService.getTotalBooking("cancelled"));
        model.addAttribute("todayCancelled", bookingService.getTodayBooking("cancelled"));
        model.addAttribute("checkInToday", bookingService.getTodayCheckIn());
        model.addAttribute("checkInFuture", bookingService.getFutureCheckIn());
        model.addAttribute("checkOutToday", bookingService.getTodayCheckOut());
        model.addAttribute("checkOutFuture", bookingService.getFutureCheckOut());

        return "admin/admin-booking-list";
    }

    @GetMapping("/admin/bookings/detail/{id}")
    public String showBookingDetail(@PathVariable("id") int id, Model model) {
        Booking booking = bookingService.findById(id);
        if (booking == null) {
            return "redirect:/admin-booking-list?error=notfound";
        }

        model.addAttribute("booking", booking);
        return "admin/admin-booking-detail";
    }

}
