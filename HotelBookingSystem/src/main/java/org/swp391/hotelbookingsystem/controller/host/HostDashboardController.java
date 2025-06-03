package org.swp391.hotelbookingsystem.controller.host;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.*;

import java.util.List;


@Controller
public class HostDashboardController {

    @Autowired
    RoomTypeService roomTypeService;

    @Autowired
    LocationService locationService;

    @Autowired
    AmenityService amenityService;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    RoomService roomService;
    @Autowired
    HotelService hotelService;

    @Autowired
    UserService userService;

    @GetMapping("/host-dashboard")
    public String showHostDashboard(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");
//        if (host==null ||!host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
//            return "redirect:/login"; // Or handle unauthorized access
//        }

        return "host/host-dashboard";
    }


}
