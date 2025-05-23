package org.swp391.hotelbookingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/", "/home"})
    public String home() {
        return "page/homepage";
    }
}
