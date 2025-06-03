package org.swp391.hotelbookingsystem.controller.host;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.EmailService;
import org.swp391.hotelbookingsystem.service.HotelService;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/host")
public class HotelDeleteController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbc;

    @PostMapping("/request-delete-hotel")
    public String requestHotelDeletion(
            @RequestParam("hotelId") int hotelId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) { // Removed throws MessagingException, handle it inside
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
            String token = otp + ":" + hotelId;  // Store both OTP and hotelId
            String tokenType = "hotel delete";
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10); // OTP valid for 10 minutes

            jdbc.update("INSERT INTO Tokens (user_id, token, expiry_date, token_type) VALUES (?, ?, ?, ?)",
                    user.getId(), token, Timestamp.valueOf(expiry), tokenType);

            emailService.sendHotelDeleteConfirmationEmail(user.getEmail(), hotel.getHotelName(), otp);

            // This is the key change: signal to show OTP modal for this hotelId
            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("message", "Mã xác nhận đã được gửi tới email của bạn. Vui lòng nhập mã OTP để hoàn tất.");
        } catch (MessagingException e) {
            // Log the exception
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi email xác nhận: " + e.getMessage());
        } catch (Exception e) {
            // Log the exception
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi yêu cầu xóa khách sạn: " + e.getMessage());
        }
        return "redirect:/host-listing";
    }

    @PostMapping("/confirm-delete-hotel")
    public String confirmHotelDeletion(
            @RequestParam("otp") String otp,
            @RequestParam("hotelId") int hotelId, // Make sure this is submitted by the OTP form
            HttpSession session, // Add session to check user
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/host-listing"; // Or appropriate error page
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách sạn hoặc bạn không phải chủ sở hữu.");
            return "redirect:/host-listing";
        }

        try {
            String tokenToVerify = otp + ":" + hotelId;
            // Important: Use GETDATE() for SQL Server, NOW() for MySQL/PostgreSQL
            // Make sure your Tokens table has a user_id column that matches the user trying to delete
            String sql = "SELECT token_type FROM Tokens WHERE token = ? AND user_id = ? AND expiry_date > GETDATE()"; // Assuming SQL Server
            String tokenType = jdbc.queryForObject(sql, String.class, tokenToVerify, user.getId());

            if (!"hotel delete".equals(tokenType)) { // queryForObject throws EmptyResultDataAccessException if no row found
                redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ, đã hết hạn, hoặc không dành cho bạn.");
                // Optionally, to make the modal reappear with an error for the same hotelId:
                redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
                redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return "redirect:/host-listing";
            }

            hotelService.deleteById(hotelId); // This should also handle related entities if cascaded or done in service
            jdbc.update("DELETE FROM Tokens WHERE token = ? AND user_id = ?", tokenToVerify, user.getId());

            redirectAttributes.addFlashAttribute("message", "Khách sạn '" + hotel.getHotelName() + "' đã được xóa thành công.");

        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            // Optionally, make the modal reappear for the same hotelId
            redirectAttributes.addFlashAttribute("showOtpModalForHotelId", hotelId);
            redirectAttributes.addFlashAttribute("otpError", "Mã OTP không hợp lệ hoặc đã hết hạn.");
        } catch (Exception e) {
            // Log the exception
            redirectAttributes.addFlashAttribute("error", "Xóa khách sạn thất bại: " + e.getMessage());
        }

        return "redirect:/host-listing";
    }
}