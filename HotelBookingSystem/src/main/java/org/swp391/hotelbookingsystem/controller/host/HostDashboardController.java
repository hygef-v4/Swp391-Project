package org.swp391.hotelbookingsystem.controller.host;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class HostDashboardController {

    @Value("${app.base-url}")
    private String baseUrl;

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

    final
    NotificationService notificationService;

    public HostDashboardController(LocationService locationService, AmenityService amenityService, CloudinaryService cloudinaryService, RoomService roomService, HotelService hotelService, UserService userService, BookingService bookingService, ReviewService reviewService, NotificationService notificationService) {
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.cloudinaryService = cloudinaryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
    }

    @GetMapping("/host-dashboard")
    public String showHostDashboard(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);

        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);


        model.addAttribute("totalHotels", hotelService.getHotelsByHostId(host.getId()).size());
        double totalRevenue = bookingService.getTotalRevenueByHostId(host.getId());
        model.addAttribute("totalRevenue", formatRevenue(totalRevenue));
        model.addAttribute("totalBookings", bookingService.countTotalBookingsByHostId(host.getId()));

        double averageRating = reviewService.getAverageHotelRatingForHost(host.getId());
        model.addAttribute("averageRating", String.format("%.1f", averageRating));


        // For recent bookings table with calculated statuses
        List<Booking> allBookings = bookingService.getBookingsByHostId(host.getId());
        
        // Filter out bookings with only pending booking units and calculate status
        List<Booking> bookings = allBookings.stream()
                .filter(booking -> {
                    if (booking.getBookingUnits() == null || booking.getBookingUnits().isEmpty()) {
                        return false;
                    }

                    // Check if booking has at least one non-pending booking unit
                    boolean hasNonPendingUnit = booking.getBookingUnits().stream()
                            .anyMatch(unit -> {
                                String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                return !"pending".equals(unitStatus);
                            });

                    if (hasNonPendingUnit) {
                        // Filter out pending booking units for this booking
                        List<BookingUnit> nonPendingUnits = booking.getBookingUnits().stream()
                                .filter(unit -> {
                                    String unitStatus = (unit.getStatus() != null) ? unit.getStatus().toLowerCase() : "";
                                    return !"pending".equals(unitStatus);
                                })
                                .collect(Collectors.toList());

                        // Set the filtered booking units
                        booking.setBookingUnits(nonPendingUnits);

                        // Calculate overall status based on non-pending units
                        String overallStatus = bookingService.calculateBookingStatus(nonPendingUnits);
                        booking.setStatus(overallStatus);

                        // Calculate number of nights for correct pricing
                        booking.calculateNumberOfNights();

                        return true;
                    }

                    return false; // Hide bookings with only pending units
                })
                .collect(Collectors.toList());
        
        model.addAttribute("bookings", bookings);

        return "host/host-dashboard";
    }

    @PostMapping("/api/host/reject-booking")
    @ResponseBody
    public Map<String, Object> rejectBooking(
            @RequestParam int bookingId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("success", false);
                response.put("message", "Không có quyền truy cập");
                return response;
            }

            Booking booking = bookingService.findById(bookingId);
            if (booking == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy booking");
                return response;
            }

            log.info("Fetched booking {} - totalPrice: {}, checkIn: {}, partialRefundDay: {}, partialRefundPercent: {}, noRefund: {}",
                booking.getBookingId(), booking.getTotalPrice(), booking.getCheckIn(),
                booking.getPartialRefundDay(), booking.getPartialRefundPercent(), booking.getNoRefund());

            // Check if booking belongs to host
            List<Hotel> hostHotels = hotelService.getHotelsByHostId(host.getId());
            boolean isHostHotel = hostHotels.stream().anyMatch(h -> h.getHotelId() == booking.getHotelId());
            if (!isHostHotel) {
                response.put("success", false);
                response.put("message", "Không có quyền chỉnh sửa booking này");
                return response;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", String.valueOf(booking.getBookingId()));
            params.add("trantype", "02");
            params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
            params.add("refundRole", "Hotel Owner");
            params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            String res = restTemplate.postForObject(baseUrl + "/refund", request, String.class);
            
            if(res != null && res.equals("00")){
                // Get original total price directly from database to ensure accuracy
                Double originalTotalPriceFromDB = bookingService.getOriginalTotalPrice(booking.getBookingId());
                double originalTotalPrice = originalTotalPriceFromDB != null ? originalTotalPriceFromDB : 0.0;

                // Host-initiated rejection = 100% refund (ignore cancellation policy)
                long refundAmount = booking.hostInitiatedRefundAmount(originalTotalPrice);

                log.info("Host-initiated booking rejection for booking {}", booking.getBookingId());
                log.info("  - Original total price from DB: {}", originalTotalPrice);
                log.info("  - Full refund amount (100%): {}", refundAmount);

                // Only reject approved booking units, leave others unchanged
                int rejectedCount = bookingService.rejectApprovedBookingUnits(booking);
                notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), refundAmount);

                response.put("success", true);
                if (rejectedCount > 0) {
                    response.put("message", "Đã từ chối " + rejectedCount + " phòng đã được duyệt. " +
                            "Vui lòng liên hệ khách hàng để hoàn tiền. " +
                            "Các phòng đã check-in không bị ảnh hưởng.");
                    response.put("needsRefund", true);
                    response.put("rejectedCount", rejectedCount);
                } else {
                    response.put("message", "Không có phòng nào cần từ chối (chỉ từ chối được phòng đã duyệt).");
                    response.put("needsRefund", false);
                }
            }else{
                response.put("success", false);
                response.put("message", "Hoàn tiền thất bại");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi từ chối booking: " + e.getMessage());
        }
        return response;
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
            if (!List.of("approved", "completed", "cancelled", "rejected", "check_in").contains(status.toLowerCase())) {
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

    @GetMapping("/host-earning")
    public String showHostEarning(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login";
        }

        // Get host hotels
        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);

        // Total revenue
        double totalRevenue = bookingService.getTotalRevenueByHostId(host.getId());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("formattedTotalRevenue", formatRevenue(totalRevenue));

        // Monthly revenue
        double monthlyRevenue = bookingService.getMonthlyRevenueByHostId(host.getId());
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("formattedMonthlyRevenue", formatRevenue(monthlyRevenue));

        // Paid bookings (approved, completed, check_in)
        int paidBookings = bookingService.countPaidBookingsByHostId(host.getId());
        model.addAttribute("paidBookings", paidBookings);

        // Calculate average revenue per paid booking
        double avgRevenuePerBooking = paidBookings > 0 ? totalRevenue / paidBookings : 0;
        model.addAttribute("avgRevenuePerBooking", avgRevenuePerBooking);
        model.addAttribute("formattedAvgRevenuePerBooking", formatRevenue(avgRevenuePerBooking));

        // Get recent bookings for earnings details
        List<Booking> recentBookings = bookingService.getBookingsByHostId(host.getId());

        // Process all bookings for earnings display
        List<Booking> earningBookings = recentBookings.stream()
                .filter(booking -> booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty())
                .peek(booking -> {
                    // Calculate number of nights and determine status
                    booking.calculateNumberOfNights();

                    // Use determineStatus() to get the overall booking status
                    String overallStatus = booking.determineStatus();
                    booking.setStatus(overallStatus);

                    // Only calculate total price for bookings that are not cancelled or rejected
                    if ("cancelled".equals(overallStatus) || "rejected".equals(overallStatus)) {
                        booking.setTotalPrice(0.0);
                        log.debug("Booking {} has status '{}' - setting total price to 0",
                            booking.getBookingId(), overallStatus);
                    } else {
                        // Use calculateTotalPrice() method which properly calculates: price * quantity * numberOfNights
                        double calculatedPrice = booking.calculateTotalPrice();
                        booking.setTotalPrice(calculatedPrice);
                        log.debug("Booking {} has status '{}' - calculated price: {}",
                            booking.getBookingId(), overallStatus, calculatedPrice);
                    }
                })
                .collect(Collectors.toList());

        model.addAttribute("earningBookings", earningBookings);

        return "host/host-earning";
    }

    @GetMapping("/api/host/test-refund/{bookingId}")
    @ResponseBody
    public Map<String, Object> testRefundCalculation(@PathVariable int bookingId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get booking from database
            Booking booking = bookingService.findById(bookingId);
            if (booking == null) {
                result.put("error", "Booking not found");
                return result;
            }

            // Get original total price directly from database
            Double originalTotalPriceFromDB = bookingService.getOriginalTotalPrice(bookingId);

            // Calculate refund (customer-initiated vs host-initiated)
            long customerRefund = booking.refundAmountWithOriginalPrice(originalTotalPriceFromDB);
            long hostRefund = booking.hostInitiatedRefundAmount(originalTotalPriceFromDB);

            // Get days until check-in
            long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDateTime.now().toLocalDate(),
                booking.getCheckIn().toLocalDate()
            );

            result.put("bookingId", bookingId);
            result.put("totalPriceFromObject", booking.getTotalPrice());
            result.put("totalPriceFromDB", originalTotalPriceFromDB);
            result.put("checkInDate", booking.getCheckIn().toString());
            result.put("daysUntilCheckIn", daysUntilCheckIn);
            result.put("partialRefundDay", booking.getPartialRefundDay());
            result.put("partialRefundPercent", booking.getPartialRefundPercent());
            result.put("noRefund", booking.getNoRefund());
            result.put("customerInitiatedRefund", customerRefund);
            result.put("hostInitiatedRefund", hostRefund);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/api/host/revenue-stats")
    @ResponseBody
    public List<Map<String, Object>> getRevenueStats(HttpSession session, @RequestParam String period) {
        User host = (User) session.getAttribute("user");
        if (host == null) {
            return List.of();
        }
        return bookingService.getRevenueStatsByHostId(host.getId(), period);
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
