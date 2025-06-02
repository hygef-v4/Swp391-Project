package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.*;

import java.util.*;

@Controller
public class AdminHotelController {

    @Autowired
    HotelService hotelService;

    @Autowired
    RoomService roomService;

    @Autowired
    ReviewService reviewService;

    @Autowired
    UserService userService;

    @Autowired
    LocationService locationService;

    @GetMapping("/admin-hotel-list")
    public String getHotelDashboard(@RequestParam(value = "search", required = false) String search, Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking - Hotel Management");
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        List<Hotel> hotels;
        if (search != null && !search.trim().isEmpty()) {
            hotels = hotelService.searchHotel(search);
        } else {
            hotels = hotelService.getAllHotels();
        }

        model.addAttribute("search", search);
        model.addAttribute("hotelList", hotels);

        return "page/admin-hotel-list";
    }
}