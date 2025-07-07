package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.*;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;

import java.util.*;

@Controller
public class AdminBookingController {

    private final BookingService bookingService;
    private final HotelService hotelService;
    private final UserService userService;

    public AdminBookingController(BookingService bookingService, HotelService hotelService, UserService userService) {
        this.bookingService = bookingService;
        this.hotelService = hotelService;
        this.userService = userService;
    }

    @GetMapping("/admin-booking-list")
    public String showBookingList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "list") String view,
                                  @RequestParam(required = false) String search,
                                  Model model) {

        int offset = (page - 1) * size;

        List<Hotel> hotelList = hotelService.searchHotelsPaginated(search, offset, size);
        int totalHotels = hotelService.countHotelsBySearch(search);
        int totalHotelPages = (int) Math.ceil((double) totalHotels / size);

        List<Booking> bookings = bookingService.getBookingsByStatusAndSearchPaginated(status, search, page - 1, size);
        int totalBookingPages = bookingService.getTotalPagesByStatusAndSearch(status, search, size);

        model.addAttribute("hotelList", hotelList);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalHotelPages", totalHotelPages);
        model.addAttribute("totalBookingPages", totalBookingPages);
        model.addAttribute("statusFilter", status);
        model.addAttribute("viewMode", view);
        model.addAttribute("search", search);

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

    @GetMapping("/admin/bookings")
    public String showBookingsOfHotel(@RequestParam("hotelId") int hotelId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String search,
                                      Model model) {

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) {
            model.addAttribute("error", "hotelNotFound");
            return "redirect:/admin-booking-list?error=hotelNotFound";
        }

        List<Booking> bookings = bookingService.getBookingsByHotelIdPaginated(hotelId, page - 1, size);
        if (bookings == null || bookings.isEmpty()) {
            model.addAttribute("error", "noBookingFound");
            return "redirect:/admin-booking-list?error=noBookingFound";
        }

        int totalBookings = bookingService.countBookingsByHotelId(hotelId);
        int totalBookingPages = (int) Math.ceil((double) totalBookings / size);

        Booking booking = bookingService.getBookingsByHotelIdOrderByDate(hotelId).get(0);
        User customer = userService.findUserById(booking.getCustomerId());
        List<BookingUnit> bookingUnit = booking.getBookingUnits();

        model.addAttribute("hotel", hotel);
        model.addAttribute("booking", booking);
        model.addAttribute("customer", customer);
        model.addAttribute("bookingUnit", bookingUnit);
        model.addAttribute("bookingList", bookings);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalBookingPages", totalBookingPages);
        model.addAttribute("hotelId", hotelId);

        return "admin/admin-booking-detail";
    }

}
