package org.swp391.hotelbookingsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
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
        Context context = new Context();     //Context holds all the dynamic variables you want to inject into the HTML template.
        context.setVariable("hotelName", hotelName);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("email/confirm-hotel-delete", context);  // Inject the variables from context into the HTML template and return the final HTML string

        MimeMessage message = mailSender.createMimeMessage();  // Creates email object
        MimeMessageHelper helper = new MimeMessageHelper(message, true);   // Helps build a multipart (HTML-capable) email
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận xóa khách sạn: " + hotelName);
        helper.setText(htmlContent, true);  // Sets the email body as HTML

        mailSender.send(message); // Opens a connection to Gmail SMTP at smtp.gmail.com:587
    }

}
