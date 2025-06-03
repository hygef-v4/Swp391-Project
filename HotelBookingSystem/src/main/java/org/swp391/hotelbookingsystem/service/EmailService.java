package org.swp391.hotelbookingsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendResetPasswordEmail(String to, String token) throws MessagingException {
        String resetLink = "http://localhost:8386/resetPassword?token=" + token;

        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String htmlContent = templateEngine.process("email/resetPasswordEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Reset your password");
        helper.setText(htmlContent, true);
        helper.setFrom("your_email@gmail.com");

        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác minh email - Mã OTP của bạn");
        message.setText("Xin chào,\n\nMã OTP xác thực email của bạn là: " + otp + "\nMã này sẽ hết hạn sau 5 phút.\n\nTrân trọng.");
        mailSender.send(message);
    }

    public void sendHotelDeleteConfirmationEmail(String toEmail, String hotelName, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("hotelName", hotelName);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("email/confirm-hotel-delete", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận xóa khách sạn: " + hotelName);
        helper.setText(htmlContent, true);
        helper.setFrom("your_email@gmail.com");

        mailSender.send(message);
    }

}
