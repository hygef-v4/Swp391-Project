package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CompareController {
    @GetMapping("/hotel-compare")
    public String hotelCompareDemo() {
        return "page/hotelCompare";
    }
} 
