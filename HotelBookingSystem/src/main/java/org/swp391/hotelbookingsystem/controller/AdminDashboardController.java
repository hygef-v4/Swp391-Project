package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.AdminDashboardService;
import org.swp391.hotelbookingsystem.service.HotelService;

import java.util.*;

@Controller
public class AdminDashboardController {
    @Autowired
    AdminDashboardService service;

    @GetMapping("/admin-dashboard")
    public String getDashboard(Model model) {
        model.addAttribute("numberOfHotels", service.countHotels());
        model.addAttribute("totalRooms", service.countRooms());
        model.addAttribute("totalBookedRooms", service.countBookedRooms());
        model.addAttribute("availableRooms", service.countAvailableRooms());
        model.addAttribute("topPopularHotels", service.getTopPopularHotels());
        model.addAttribute("recentRoomBookings", service.getRecentRoomBookings());
        model.addAttribute("upcomingGuests", service.getUpcomingGuests());
        model.addAttribute("recentReviews", service.getRecentReviews());

        Map<String, Integer> checkInData = service.getCheckInChartDataByDate();
        Map<String, Integer> checkOutData = service.getCheckOutChartDataByDate();

        model.addAttribute("dateLabels", new ArrayList<>(checkInData.keySet()));
        model.addAttribute("checkInData", new ArrayList<>(checkInData.values()));
        model.addAttribute("checkOutData", new ArrayList<>(checkOutData.values()));

        return "page/adminDashboard";
    }
}
