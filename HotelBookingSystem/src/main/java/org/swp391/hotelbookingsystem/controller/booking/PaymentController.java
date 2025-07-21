package org.swp391.hotelbookingsystem.controller.booking;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.VNPayService;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {
    @Autowired
    BookingService bookingService;
    @Autowired
    UserService userService;
    @Autowired
    HotelService hotelService;
    @Autowired
    VNPayService vnpayService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    SpringTemplateEngine templateEngine;

    @GetMapping("/payment/{id}")
    public String payment(
        @PathVariable(value = "id") int id,

        @RequestParam(value = "hotelId") int hotelId,
        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int rooms,

        HttpSession session, Model model
    ){
        model.addAttribute("id", id);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("dateRange", dateRange);
        model.addAttribute("guests", guests);
        model.addAttribute("rooms", rooms);
        
        Booking booking = bookingService.findById(id);
        if(booking == null) {
            return "redirect:/booking/" + hotelId + "?dateRange=" + dateRange + "&guests=" + guests + "&rooms=" + rooms;
        }

        User user = (User) session.getAttribute("user");
        if(user == null || user.getId() != booking.getCustomerId()) {
            return "redirect:/login";
        }

        double price = 0;
        for(BookingUnit bookingUnit : booking.getBookingUnits()) {
            price += bookingUnit.getPrice() * bookingUnit.getQuantity();
        }long night = ChronoUnit.DAYS.between(booking.getCheckIn().toLocalDate(), booking.getCheckOut().toLocalDate());
        double discount = price * night - booking.getTotalPrice();

        model.addAttribute("id", id);
        model.addAttribute("booking", booking);
        model.addAttribute("price", price);
        model.addAttribute("night", night);
        model.addAttribute("discount", discount);

        int remainTime = (60 * 15) - bookingService.remainPendingTime(id);
        if(remainTime <= 0) remainTime = 0;
        
        model.addAttribute("remainMinute", String.format("%02d", remainTime / 60));
        model.addAttribute("remainSecond", String.format("%02d", remainTime % 60));

        return "page/payment";
    }

    @PostMapping("/create-payment")
    public String createPayment(
        @RequestParam("id") int id,

        @RequestParam("amount") long amount, 
        @RequestParam("orderInfo") String orderInfo,

        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int rooms,
        
        HttpServletRequest request
    ){
        try{
            String returnUrl = "/booking-success/" + id + "?dateRange=" + dateRange + "&guests=" + guests + "&rooms=" + rooms;
            String paymentUrl = vnpayService.createPayment(amount, orderInfo, returnUrl, request);
            return "redirect:" + paymentUrl;
        }catch(Exception e){
            return "redirect:/error";
        }
    }

    @GetMapping("/booking-success/{id}")
    public String bookingSuccess(
        @PathVariable(value = "id") int id,

        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int rooms,    

        HttpServletRequest request, HttpSession session, Model model
    ){
        try{
            boolean paymentStatus = vnpayService.returnPayment(request);
            if(!paymentStatus) return "redirect:/booking-error/" + id + "?dateRange=" + dateRange + "&guests=" + guests + "&rooms=" + rooms;

            Booking booking = bookingService.findById(id);
            if(booking == null) return "redirect:/";

            User user = (User) session.getAttribute("user");
            if(user == null || user.getId() != booking.getCustomerId()) {
                return "redirect:/login";
            }

            int approved = bookingService.approveBooking(id, request.getParameter("vnp_TxnRef"), request.getParameter("vnp_TransactionNo"), request.getParameter("vnp_PayDate"));
            if(approved > 0){
                notificationService.notifyBookingConfirmation(user.getId(), String.valueOf(id), booking.getHotelName());
            }

            model.addAttribute("booking", booking);

            model.addAttribute("dateRange", dateRange);
            model.addAttribute("guests", guests);
            model.addAttribute("rooms", rooms);

            return "page/bookingSuccess";
        }catch(Exception e){
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    @GetMapping("/booking-error/{id}")
    public String bookingError(
        @PathVariable(value = "id") int id,

        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int rooms,    

        HttpSession session, Model model
    ){
        Booking booking = bookingService.findById(id);
        if(booking == null) return "redirect:/";

        User user = (User) session.getAttribute("user");
        if(user == null || user.getId() != booking.getCustomerId()) {
            return "redirect:/login";
        }

        bookingService.deletePendingBooking(id, user.getId());
        model.addAttribute("id", booking.getHotelId());

        model.addAttribute("dateRange", dateRange);
        model.addAttribute("guests", guests);
        model.addAttribute("rooms", rooms);

        return "page/bookingError";
    }

    @PostMapping("/invoice")
    public void invoice(@RequestParam("id") int id, HttpServletResponse response) throws IOException{
        // 1. Load HTML template
        Context context = new Context();

        Booking booking = bookingService.findById(id);
        User user = userService.findUserById(booking.getCustomerId());
        Hotel hotel = hotelService.getHotelById(booking.getHotelId());

        context.setVariable("booking", booking);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        context.setVariable("createdAt", booking.getCreatedAt().format(formatter));
        context.setVariable("checkIn", booking.getCheckIn().format(formatter));
        context.setVariable("checkOut", booking.getCheckOut().format(formatter));

        context.setVariable("user", user);
        context.setVariable("hotel", hotel);

        String html = templateEngine.process("pdf/pdf", context);

        // 2. Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=booking.pdf");

        // 3. Convert HTML to PDF
        try (OutputStream os = response.getOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, new File("src/main/resources/static/").toURI().toString());
            builder.useFont(new File("src/main/resources/static/assets/font/DejaVuSans.ttf"), "DejaVu Sans");
            builder.toStream(os);
            builder.run();
        }
    }
}
