package org.swp391.hotelbookingsystem.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.swp391.hotelbookingsystem.service.SiteSettingsService;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;
    private final SiteSettingsService siteSettingsService;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine, SiteSettingsService siteSettingsService) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.siteSettingsService = siteSettingsService;
    }

    public void sendResetPasswordEmail(String to, String resetLink) throws MessagingException {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String htmlContent = templateEngine.process("email/resetPasswordEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu của bạn");
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác minh email - Mã OTP của bạn");
        message.setText("Xin chào,\n\nMã OTP xác thực email của bạn là: " + otp + "\nMã này sẽ hết hạn sau 5 phút.\n\nTrân trọng.");
        message.setFrom(siteSettingsService.getSettings().getSupportEmail());
        mailSender.send(message);
    }

    public void sendHotelDeleteConfirmationEmail(String toEmail, String hotelName, String otp) throws MessagingException {
        Context context = new Context();     //Context holds all the dynamic variables you want to inject into the HTML template.
        context.setVariable("hotelName", hotelName);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("email/confirm-hotel-delete", context);  // Inject the variables from context into the HTML template and return the final HTML string

        MimeMessage message = mailSender.createMimeMessage();  // Creates email object
        MimeMessageHelper helper = new MimeMessageHelper(message, true);   // Helps build a multipart (HTML-capable) email
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận xóa khách sạn: " + hotelName);
        helper.setText(htmlContent, true);  // Sets the email body as HTML
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message); // Opens a connection to Gmail SMTP at smtp.gmail.com:587
    }

    public void sendHotelDeactivateConfirmationEmail(String toEmail, String hotelName, String otp) throws MessagingException {
        Context context = new Context();     //Context holds all the dynamic variables you want to inject into the HTML template.
        context.setVariable("hotelName", hotelName);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("email/confirm-hotel-deactivate", context);  // Inject the variables from context into the HTML template and return the final HTML string

        MimeMessage message = mailSender.createMimeMessage();  // Creates email object
        MimeMessageHelper helper = new MimeMessageHelper(message, true);   // Helps build a multipart (HTML-capable) email
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận vô hiệu hóa khách sạn: " + hotelName);
        helper.setText(htmlContent, true);  // Sets the email body as HTML
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message); // Opens a connection to Gmail SMTP at smtp.gmail.com:587
    }

    public void sendHotelForceDeactivateConfirmationEmail(String toEmail, String hotelName, String otp, int activeBookingsCount) throws MessagingException {
        Context context = new Context();
        context.setVariable("hotelName", hotelName);
        context.setVariable("otp", otp);
        context.setVariable("activeBookingsCount", activeBookingsCount);

        String htmlContent = templateEngine.process("email/confirm-hotel-force-deactivate", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("⚠️ Xác nhận vô hiệu hóa khách sạn có đặt phòng đang hoạt động - " + hotelName);
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }

    public void sendContactMessage(String name, String email, String phone, String messageContent) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", email);
        context.setVariable("phone", phone);
        context.setVariable("messageContent", messageContent);

        String htmlContent = templateEngine.process("email/contact-message", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(siteSettingsService.getSettings().getSupportEmail());
        helper.setSubject("Tin nhắn từ biểu mẫu liên hệ");
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }

    public void sendUserBanEmail(String to, String reason) throws MessagingException {
        Context context = new Context();
        context.setVariable("reason", reason);
        String htmlContent = templateEngine.process("email/user-ban-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Tài khoản của bạn đã bị khóa");
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }

    public void sendUserUnbanEmail(String to, String reason) throws MessagingException {
        Context context = new Context();
        context.setVariable("reason", reason);
        String htmlContent = templateEngine.process("email/user-unban-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Tài khoản của bạn đã được mở khóa");
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }

    public void sendChangePasswordConfirmationEmail(String toEmail) throws MessagingException {
        Context context = new Context();
        // Có thể thêm biến động nếu muốn cá nhân hóa
        String htmlContent = templateEngine.process("email/change-password-confirmation", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Xác nhận thay đổi mật khẩu");
        helper.setText(htmlContent, true);
        helper.setFrom(siteSettingsService.getSettings().getSupportEmail());

        mailSender.send(message);
    }


}
