package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingHistoryController {

    private final BookingService bookingService;

    public BookingHistoryController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

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

    @GetMapping("/booking-detail/{id}")
    public String getBookingDetail(@PathVariable int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Booking booking = bookingService.findById(id);
        String image = bookingService.getImageByBookingId(booking.getBookingId());
        booking.setImageUrl(image);
        model.addAttribute("booking", booking);
        String hotelName = bookingService.getHotelNameByBookingId(booking.getBookingId());
        booking.setHotelName(hotelName);
        String roomName = bookingService.getRoomNameByBookingId(booking.getBookingId());
        booking.setRoomName(roomName);
        boolean isCancelable = false;
        if ("approved".equalsIgnoreCase(booking.getStatus()) && booking.getCheckIn().isAfter(LocalDate.now().atStartOfDay())) {
            isCancelable = true;
        }
        model.addAttribute("isCancelable", isCancelable);
        return "page/bookingDetail";
    }

    @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable("id") int bookingId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId);
        if (booking != null && booking.getCustomerId() == user.getId()) {
            if ("approved".equalsIgnoreCase(booking.getStatus()) && booking.getCheckIn().isAfter(LocalDate.now().atStartOfDay())) {
                bookingService.updateStatus(booking, "cancelled");
            }
        }

        return "redirect:/bookingHistory";
    }

}
