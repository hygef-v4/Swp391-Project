package org.swp391.hotelbookingsystem.controller.host;

import java.time.LocalDateTime;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.HotelService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/host")
public class HotelDeactivateController {

    private final HotelService hotelService;

    private final EmailService emailService;

    private final BookingService bookingService;

    public HotelDeactivateController(HotelService hotelService, EmailService emailService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.emailService = emailService;
        this.bookingService = bookingService;
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

            if (!"hotel deactivate".equals(tokenType)) {
                redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ, đã hết hạn, hoặc không dành cho bạn.");
                redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
                redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return "redirect:/host-listing";
            }

            // Reject all active bookings first
            int rejectedBookings = bookingService.rejectAllActiveBookingsByHotelId(hotelId);

            // Then deactivate the hotel
            hotelService.updateHotelStatus(hotelId, "inactive");

            String message = "Khách sạn '" + hotel.getHotelName() + "' đã được vô hiệu hóa thành công.";
            if (rejectedBookings > 0) {
                message += " Đã hủy " + rejectedBookings + " đặt phòng đang hoạt động. Vui lòng liên hệ với khách hàng để hoàn tiền.";
            }
            redirectAttributes.addFlashAttribute("message", message);
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
            // Reject all active bookings first
            int rejectedBookings = bookingService.rejectAllActiveBookingsByHotelId(hotelId);

            // Then deactivate the hotel
            hotelService.updateHotelStatus(hotelId, "inactive");

            String message = "Khách sạn '" + hotel.getHotelName() + "' đã được vô hiệu hóa thành công.";
            if (rejectedBookings > 0) {
                message += " Đã hủy " + rejectedBookings + " đặt phòng đang hoạt động. Vui lòng liên hệ với khách hàng để hoàn tiền.";
            }

            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Vô hiệu hóa khách sạn thất bại: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

}
