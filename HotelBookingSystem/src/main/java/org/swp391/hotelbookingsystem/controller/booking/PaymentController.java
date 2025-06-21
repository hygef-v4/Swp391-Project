package org.swp391.hotelbookingsystem.controller.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {
    @Autowired
    VNPayService vnpayService;

    @GetMapping("/payment")
    public String payment(
        Model model, HttpSession session
    ){
        Booking booking = (Booking) session.getAttribute("booking");
        if(booking == null) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        if(user == null || user.getId() != booking.getCustomerId()) {
            return "redirect:/login";
        }

        double price = 0;
        for(BookingUnit bookingUnit : booking.getBookingUnits()) {
            price += bookingUnit.getPrice() * bookingUnit.getQuantity();
        }double discount = booking.getTotalPrice() - price;

        model.addAttribute("booking", booking);
        model.addAttribute("price", price);
        model.addAttribute("discount", discount);

        return "page/payment";
    }

    @PostMapping("/create-payment")
    public String createPayment(@RequestParam("amount") long amount, @RequestParam("orderInfo") String orderInfo, HttpServletRequest request){
        try{
            String paymentUrl = vnpayService.createPayment(amount, orderInfo, request);
            return "redirect:" + paymentUrl;
        }catch(Exception e){
            return "redirect:/error";
        }
    }

    @GetMapping("/return-payment")
    public String returnPayment(HttpServletRequest request, Model model){
        try{
            boolean paymentStatus = vnpayService.returnPayment(request);
            return "redirect:" + (paymentStatus ? "/" : "/error");
        }catch(Exception e){
            return "redirect:/error";
        }
    }
}
