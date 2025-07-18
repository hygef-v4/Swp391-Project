package org.swp391.hotelbookingsystem.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentInformationController {


    @GetMapping("payment-information")
    public String getMethodName(@RequestParam String param) {
        return "page/paymentInformation.html";
    }
    
}
