package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
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
    public String bookingHistory(
            HttpSession session,
            Model model,
            @RequestParam(value = "tab", defaultValue = "upcoming") String tab,
            @RequestParam(value = "pageUpcoming", defaultValue = "1") int pageUpcoming,
            @RequestParam(value = "pageCancelled", defaultValue = "1") int pageCancelled,
            @RequestParam(value = "pageCompleted", defaultValue = "1") int pageCompleted,
            @RequestParam(value = "pageCheckin", defaultValue = "1") int pageCheckin,
            @RequestParam(value = "size", defaultValue = "3") int size
    ) {
        User user = (User) session.getAttribute("user");
        int customerId = user.getId();

        List<Booking> upcomingBookings = bookingService.getUpcomingBookingsPaginated(customerId, pageUpcoming - 1, size);
        List<Booking> cancelledBookings = bookingService.getCancelledBookingsPaginated(customerId, pageCancelled - 1, size);
        List<Booking> completedBookings = bookingService.getCompletedBookingsPaginated(customerId, pageCompleted - 1, size);
        List<Booking> checkinBookings = bookingService.getCheckinBookingsPaginated(customerId, pageCheckin - 1, size);


        int totalPagesUpcoming = bookingService.getTotalPagesUpcoming(customerId, size);
        int totalPagesCancelled = bookingService.getTotalPagesCancelled(customerId, size);
        int totalPagesCompleted = bookingService.getTotalPagesCompleted(customerId, size);
        int totalPagesCheckin = bookingService.getTotalPagesCheckin(customerId, size);

        int totalUpcomingBookings = bookingService.getTotalUpcomingBookings(customerId);
        int totalCancelledBookings = bookingService.getTotalCancelledBookings(customerId);
        int totalCompletedBookings = bookingService.getTotalCompletedBookings(customerId);
        int totalCheckinBookings = bookingService.getTotalCheckinBookings(customerId);

        model.addAttribute("totalUpcomingBookings", totalUpcomingBookings);
        model.addAttribute("totalCancelledBookings", totalCancelledBookings);
        model.addAttribute("totalCompletedBookings", totalCompletedBookings);
        model.addAttribute("totalCheckinBookings", totalCheckinBookings);

        model.addAttribute("tab", tab);

        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("checkinBookings", checkinBookings);

        model.addAttribute("currentPageUpcoming", pageUpcoming);
        model.addAttribute("currentPageCancelled", pageCancelled);
        model.addAttribute("currentPageCompleted", pageCompleted);
        model.addAttribute("currentPageCheckin", pageCheckin);

        model.addAttribute("totalPagesUpcoming", totalPagesUpcoming);
        model.addAttribute("totalPagesCancelled", totalPagesCancelled);
        model.addAttribute("totalPagesCompleted", totalPagesCompleted);
        model.addAttribute("totalPagesCheckin", totalPagesCheckin);

        return "page/bookingHistory";
    }

    @GetMapping("/booking-detail/{id}")
    public String getBookingDetail(
        @PathVariable int id,
        @RequestParam(value = "upcoming", defaultValue = "false") boolean upcoming,
        @RequestParam(value = "cancelled", defaultValue = "false") boolean cancelled,
        @RequestParam(value = "completed", defaultValue = "false") boolean completed,
        @RequestParam(value = "checkin", defaultValue = "false") boolean checkin,
        Model model, HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Booking booking = bookingService.findById(id);
        model.addAttribute("booking", booking);

        model.addAttribute("upcoming", upcoming);
        model.addAttribute("cancelled", cancelled);
        model.addAttribute("completed", completed);
        model.addAttribute("checkin", checkin);


        for (BookingUnit bookingUnit : booking.getBookingUnits()) {
            bookingUnit.setCancelable("approved".equalsIgnoreCase(bookingUnit.getStatus()));
        }
        
        return "page/bookingDetail";
    }

    @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable("id") int bookingId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        BookingUnit bookingUnit = bookingService.findBookingUnitById(bookingId);
        Booking booking = bookingService.findBooking(bookingId);
        if (bookingUnit != null && booking.getCustomerId() == user.getId()) {
            if ("approved".equalsIgnoreCase(bookingUnit.getStatus()) && booking.getCheckIn().isAfter(LocalDate.now().atStartOfDay())) {
                bookingService.updateStatus(bookingUnit, "cancelled");
            }
        }

        return "redirect:/bookingHistory";
    }

}
