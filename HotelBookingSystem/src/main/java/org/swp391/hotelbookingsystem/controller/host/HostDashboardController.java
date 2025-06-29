package org.swp391.hotelbookingsystem.controller.host;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
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

        // if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
        //     return "redirect:/login"; // not logged in
        // }

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);

        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);


        model.addAttribute("totalHotels", hotelService.getHotelsByHostId(host.getId()).size());
        double totalRevenue = bookingService.getTotalRevenueByHostId(host.getId());
        model.addAttribute("totalRevenue", formatRevenue(totalRevenue));
        model.addAttribute("totalBookings", bookingService.countTotalBookingsByHostId(host.getId()));
//        model.addAttribute("completedBookings", bookingService.countCompletedBookingsByHostId(host.getId()));

        double averageRating = reviewService.getAverageHotelRatingForHost(host.getId());
        model.addAttribute("averageRating", String.format("%.1f", averageRating));


        // For recent bookings table with calculated statuses
        List<Booking> bookings = bookingService.getBookingsByHostId(host.getId());
        
        // Calculate overall status for each booking
        for (Booking booking : bookings) {
            String overallStatus = bookingService.calculateBookingStatus(booking.getBookingUnits());
            booking.setStatus(overallStatus);
        }
        
        model.addAttribute("bookings", bookings);

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

    @PostMapping("/api/host/update-booking-unit-status")
    @ResponseBody
    public Map<String, Object> updateBookingUnitStatus(
            @RequestParam int bookingUnitId, 
            @RequestParam String status,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("success", false);
                response.put("message", "Không có quyền truy cập");
                return response;
            }

            // Validate status
            if (!List.of("approved", "completed", "cancelled", "rejected").contains(status.toLowerCase())) {
                response.put("success", false);
                response.put("message", "Trạng thái không hợp lệ");
                return response;
            }

            // Check if booking unit belongs to host's hotel
            BookingUnit bookingUnit = bookingService.findBookingUnitById(bookingUnitId);
            if (bookingUnit == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy booking unit");
                return response;
            }

            Booking booking = bookingService.findBooking(bookingUnitId);
            if (booking == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy booking");
                return response;
            }

            // Verify the hotel belongs to this host
            List<Hotel> hostHotels = hotelService.getHotelsByHostId(host.getId());
            boolean isHostHotel = hostHotels.stream().anyMatch(h -> h.getHotelId() == booking.getHotelId());
            
            if (!isHostHotel) {
                response.put("success", false);
                response.put("message", "Không có quyền chỉnh sửa booking này");
                return response;
            }

            // Update the booking unit status
            bookingService.updateBookingUnitStatus(bookingUnitId, status);
            
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        
        return response;
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
