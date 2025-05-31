package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.dto.UserWithProfileDTO;
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
    public String getHotelDashboard(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking - Hotel Management");

        int numberOfHotels = hotelService.countHotels();
        model.addAttribute("numberOfHotels", numberOfHotels);

        List<Hotel> hotels = hotelService.getAllHotels(); // or getHotelsByLocation/filter if needed
        model.addAttribute("hotelList", hotels);

        List<User> hotelOwnerList = userService.getUsersByRole("HOTEL OWNER");
        model.addAttribute("numberOfHotelOwners", hotelOwnerList.size());

        return "page/admin-hotel-list";
    }
}