package org.swp391.hotelbookingsystem.controller.host;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.HotelService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/host")
public class HotelDeleteController {

    private final HotelService hotelService;

    private final EmailService emailService;

    public HotelDeleteController(HotelService hotelService, EmailService emailService) {
        this.hotelService = hotelService;
        this.emailService = emailService;
    }

    @PostMapping("/request-delete-hotel")
    public String requestHotelDeletion(
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
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP
            String token = otp + ":" + hotelId;
            String tokenType = "hotel delete";
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            hotelService.insertHotelDeletionToken(user.getId(), token, expiry, tokenType);
            emailService.sendHotelDeleteConfirmationEmail(user.getEmail(), hotel.getHotelName(), otp);

            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("message", "Mã xác nhận đã được gửi tới email của bạn. Vui lòng nhập mã OTP để hoàn tất.");
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi email xác nhận: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi yêu cầu xóa khách sạn: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }


    @PostMapping("/confirm-delete-hotel")
    public String confirmHotelDeletion(
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
            redirectAttributes.addFlashAttribute("message", "Yêu cầu xóa khách sạn đã được hủy.");
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

            if (!"hotel delete".equals(tokenType)) {
                redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ, đã hết hạn, hoặc không dành cho bạn.");
                redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
                redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return "redirect:/host-listing";
            }

            hotelService.deleteById(hotelId);


            redirectAttributes.addFlashAttribute("message", "Khách sạn '" + hotel.getHotelName() + "' đã được xóa thành công.");
        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa khách sạn thất bại: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }

}
