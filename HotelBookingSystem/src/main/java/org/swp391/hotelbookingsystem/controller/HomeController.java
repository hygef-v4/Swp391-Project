package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.service.LocationService;

import java.util.List;

@Controller
public class HomeController {
@Autowired
LocationService locationService;

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking");
        List<Location> locations = locationService.getAllLocations();
        session.setAttribute("locations", locations);

        return "page/homepage";
    }
}
