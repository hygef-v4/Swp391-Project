package org.swp391.hotelbookingsystem.controller.admin;

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
    public String getHotelDashboard(@RequestParam(value = "search", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    Model model, HttpSession session) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking - Hotel Management");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String trimmedSearch = (search != null) ? search.trim().replaceAll("\\s+", " ") : null;

        List<Hotel> filteredHotels;
        if (trimmedSearch != null && !trimmedSearch.isEmpty()) {
            filteredHotels = hotelService.searchHotel(search);
        } else {
            filteredHotels = hotelService.getAllHotels();
        }

        int pageSize = 10;
        int totalHotels = filteredHotels.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalHotels);
        List<Hotel> currentHotels = (startIndex < totalHotels) ? filteredHotels.subList(startIndex, endIndex) : List.of();

        model.addAttribute("search", trimmedSearch);
        model.addAttribute("hotelList", currentHotels);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalHotels / pageSize));
        model.addAttribute("startIndex", startIndex); // +1 to match display
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalHotels", totalHotels);


        return "admin/admin-hotel-list";
    }
}