package org.swp391.hotelbookingsystem.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Bank;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BankService;

import jakarta.servlet.http.HttpSession;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentInformationController {
    @Autowired
    private BankService bankService;

    @GetMapping("/payment-information")
    public String paymentInformation(Model model, HttpSession session,
                                   @RequestParam(required = false) String incomplete,
                                   @RequestParam(required = false) String reason){
        List<Bank> banks = bankService.getAllBank();
        model.addAttribute("banks", banks);

        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        user.setBanks(bankService.getUserBanks(user.getId()));
        model.addAttribute("user", user);

        // Handle redirect from hotel registration
        if ("true".equals(incomplete) && "hotel_registration".equals(reason)) {
            System.out.println("=== PAYMENT PAGE REDIRECT ===");
            System.out.println("Incomplete: " + incomplete);
            System.out.println("Reason: " + reason);
            System.out.println("Setting hotelRegistrationRedirect = true");
            model.addAttribute("hotelRegistrationRedirect", true);
            model.addAttribute("redirectMessage", "Vui lòng thêm thông tin thanh toán để tiếp tục đăng ký khách sạn.");
        }

        return "page/paymentInformation.html";
    }

    @PostMapping("/add-bank")
    public String addBank(
        @RequestParam("bankId") int bankId,
        @RequestParam("bankNumber") String bankNumber,
        @RequestParam("userName") String userName,

        HttpSession session, RedirectAttributes redirectAttributes
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        int check = bankService.addBank(user.getId(), bankId, bankNumber, userName);
        if(check == -1){
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản này đã được lưu trước đó.");
        }else{
            redirectAttributes.addFlashAttribute("successMessage", "Lưu tài khoản thành công.");

            // Check if user was redirected from hotel registration
            String returnTo = (String) session.getAttribute("returnToAfterProfileUpdate");
            if ("hotel_registration".equals(returnTo)) {
                session.removeAttribute("returnToAfterProfileUpdate");
                session.setAttribute("profileJustUpdated", "true");
                redirectAttributes.addFlashAttribute("success", "Thông tin thanh toán đã được thêm! Bạn có thể tiếp tục đăng ký khách sạn.");
                return "redirect:/register-host?from=payment";
            }
        }

        return "redirect:/payment-information";
    }

    @PostMapping("/edit-bank")
    public String editBank(
        @RequestParam("bankId") int bankId,
        @RequestParam("bankNumber") String bankNumber,
        @RequestParam("userName") String userName,

        @RequestParam("oldId") int oldId,
        @RequestParam("oldNumber") String oldNumber,

        HttpSession session, RedirectAttributes redirectAttributes
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        int check = bankService.editBank(user.getId(), bankId, bankNumber, userName, oldId, oldNumber);
        if(check == -1){
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản này đã được lưu trước đó.");
        }else{
            redirectAttributes.addFlashAttribute("successMessage", "Sửa thông tin tài khoản thành công.");
        }

        return "redirect:/payment-information";
    }

    @PostMapping("/delete-bank")
    public String deleteBank(
        @RequestParam("bankId") int bankId,
        @RequestParam("bankNumber") String bankNumber,

        HttpSession session, RedirectAttributes redirectAttributes
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        bankService.deleteBank(user.getId(), bankId, bankNumber);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công.");

        return "redirect:/payment-information";
    }
}
