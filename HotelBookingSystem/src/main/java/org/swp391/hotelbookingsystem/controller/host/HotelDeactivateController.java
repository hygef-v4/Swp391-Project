package org.swp391.hotelbookingsystem.controller.host;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/host")
public class HotelDeactivateController {

    @Value("${app.base-url}")
    private String baseUrl;

    private final HotelService hotelService;

    private final EmailService emailService;

    private final BookingService bookingService;

    private final NotificationService notificationService;

    public HotelDeactivateController(HotelService hotelService, EmailService emailService, BookingService bookingService, NotificationService notificationService) {
        this.hotelService = hotelService;
        this.emailService = emailService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @PostMapping("/request-deactivate-hotel")
    public String requestHotelDeactivation(
            @RequestParam("hotelId") int hotelId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/host-listing";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách sạn hoặc bạn không phải chủ sở hữu.");
            return "redirect:/host-listing";
        }

        // Check for active bookings
        int activeBookingsCount = bookingService.countActiveBookingsByHotelId(hotelId);
        if (activeBookingsCount > 0) {
            redirectAttributes.addFlashAttribute("hasActiveBookings", true);
            redirectAttributes.addFlashAttribute("activeBookingsCount", activeBookingsCount);
            redirectAttributes.addFlashAttribute("hotelIdWithBookings", hotelId);
            redirectAttributes.addFlashAttribute("hotelNameWithBookings", hotel.getHotelName());
            redirectAttributes.addFlashAttribute("warning",
                "Khách sạn này hiện có " + activeBookingsCount + " đặt phòng đang hoạt động (đã được duyệt hoặc đang check-in). " +
                "Nếu tiếp tục vô hiệu hóa, tất cả các đặt phòng này sẽ bị hủy và bạn sẽ phải hoàn tiền toàn bộ cho khách hàng.");
            return "redirect:/host-listing";
        }

        try {
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP
            String token = otp + ":" + hotelId;
            String tokenType = "hotel deactivate";
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            hotelService.insertHotelDeletionToken(user.getId(), token, expiry, tokenType);
            emailService.sendHotelDeactivateConfirmationEmail(user.getEmail(), hotel.getHotelName(), otp);

            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("message", "Mã xác nhận đã được gửi tới email của bạn. Vui lòng nhập mã OTP để hoàn tất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi yêu cầu vô hiệu hóa khách sạn: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

    @PostMapping("/force-deactivate-hotel")
    public String forceDeactivateHotel(
            @RequestParam("hotelId") int hotelId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/host-listing";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách sạn hoặc bạn không phải chủ sở hữu.");
            return "redirect:/host-listing";
        }

        try {
            // Generate OTP and send email for force deactivation
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP
            String token = otp + ":" + hotelId;
            String tokenType = "hotel force deactivate"; // Different token type for force deactivation
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            hotelService.insertHotelDeletionToken(user.getId(), token, expiry, tokenType);

            // Get active bookings count for email
            int activeBookingsCount = bookingService.countActiveBookingsByHotelId(hotelId);

            // Send email with warning about active bookings
            emailService.sendHotelForceDeactivateConfirmationEmail(user.getEmail(), hotel.getHotelName(), otp, activeBookingsCount);

            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("isForceDeactivate", true);
            redirectAttributes.addFlashAttribute("activeBookingsCount", activeBookingsCount);
            redirectAttributes.addFlashAttribute("message", "Mã xác nhận đã được gửi tới email của bạn. Vui lòng nhập mã OTP để hoàn tất vô hiệu hóa khách sạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi yêu cầu vô hiệu hóa khách sạn: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

    @PostMapping("/confirm-deactivate-hotel")
    public String confirmHotelDeactivation(
            @RequestParam("otp") String otp,
            @RequestParam("hotelId") int hotelId,
            @RequestParam(value = "isCancelled", defaultValue = "false") boolean isCancelled,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/host-listing";
        }

        if (isCancelled) {
            hotelService.cancelHotelDeleteToken(user.getId(), hotelId);
            redirectAttributes.addFlashAttribute("message", "Yêu cầu vô hiệu hóa khách sạn đã được hủy.");
            return "redirect:/host-listing";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách sạn hoặc bạn không phải chủ sở hữu.");
            return "redirect:/host-listing";
        }

        try {
            String tokenToVerify = otp + ":" + hotelId;
            String tokenType = hotelService.getHotelDeleteTokenType(tokenToVerify, user.getId());

            // Check if it's either normal deactivate or force deactivate
            if (!"hotel deactivate".equals(tokenType) && !"hotel force deactivate".equals(tokenType)) {
                redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ, đã hết hạn, hoặc không dành cho bạn.");
                redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
                redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return "redirect:/host-listing";
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            List<Booking> bookings = bookingService.findActiveBookingsByHotelId(hotelId);

            for(Booking booking : bookings){
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("id", String.valueOf(booking.getBookingId()));
                params.add("trantype", "02");
                params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
                params.add("refundRole", "Hotel Owner");
                params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
                String res = restTemplate.postForObject(baseUrl + "/refund", request, String.class);

                if(res != null && res.equals("00")){
                    // Reject all active bookings first (for both normal and force deactivate)
                    int rejectedBookings = bookingService.rejectAllActiveBookingsByHotelId(hotelId);

                    // Then deactivate the hotel
                    hotelService.updateHotelStatus(hotelId, "inactive");
                    notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), booking.refundAmount());

                    String message = "Khách sạn '" + hotel.getHotelName() + "' đã được vô hiệu hóa thành công.";
                    if (rejectedBookings > 0) {
                        message += " Đã hủy " + rejectedBookings + " đặt phòng đang hoạt động. Vui lòng liên hệ với khách hàng để hoàn tiền.";
                    }
                    redirectAttributes.addFlashAttribute("message", message);
                }else{
                    redirectAttributes.addFlashAttribute("error", "Hoàn tiền thất bại");
                }
            }
        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Vô hiệu hóa khách sạn thất bại: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

    @PostMapping("/activate-hotel")
    public String activateHotel(
            @RequestParam("hotelId") int hotelId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/host-listing";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách sạn hoặc bạn không phải chủ sở hữu.");
            return "redirect:/host-listing";
        }

        try {
            hotelService.updateHotelStatus(hotelId, "active");
            redirectAttributes.addFlashAttribute("message", "Khách sạn '" + hotel.getHotelName() + "' đã được kích hoạt thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kích hoạt khách sạn thất bại: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

}
