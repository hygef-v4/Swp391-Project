package org.swp391.hotelbookingsystem.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Bank;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BankService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentInformationController {
    @Autowired
    private BankService bankService;

    @GetMapping("/payment-information")
    public String paymentInformation(Model model, HttpSession session){
        List<Bank> banks = bankService.getAllBank();
        model.addAttribute("banks", banks);

        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        user.setBanks(bankService.getUserBanks(user.getId()));
        model.addAttribute("user", user);

        return "page/paymentInformation.html";
    }

    @PostMapping("/add-bank")
    public String addBank(
        @RequestParam("bankId") String bankId,
        @RequestParam("bankNumber") String bankNumber,
        @RequestParam("userName") String userName,

        HttpSession session
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        
        return "redirect:/payment-information";
    }

}
