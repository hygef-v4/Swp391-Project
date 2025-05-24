package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;

@Controller
public class HomeController {


    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking");

        return "page/homepage";
    }
}
