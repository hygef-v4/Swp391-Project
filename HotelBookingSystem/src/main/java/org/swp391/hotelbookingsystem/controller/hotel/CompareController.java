package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import java.util.List;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.service.HotelService;

@Controller
public class CompareController {
    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotel-compare")
    public String hotelCompareDemo(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        if (hotels.size() > 3) {
            hotels = hotels.subList(0, 3);
        }
        model.addAttribute("hotels", hotels);
        return "page/hotelCompare";
    }
} 
