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
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.AdminDashboardService;
import org.swp391.hotelbookingsystem.service.HotelService;

import java.util.List;

@Controller
public class AdminDashboardController {
    @Autowired
    AdminDashboardService adminDashboardService;

    @GetMapping("/admin-dashboard")
    public String showAdminDashboard(Model model, HttpSession session) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Admin Dashboard");

        int numberOfHotels = adminDashboardService.getNumberOfHotels();
        model.addAttribute("numberOfHotels", numberOfHotels);

        int totalRooms = adminDashboardService.getTotalRooms();
        model.addAttribute("totalRooms", totalRooms);

        int totalBookedRooms = adminDashboardService.getTotalBookedRooms();
        model.addAttribute("totalBookedRooms", totalBookedRooms);

        List<Hotel> topPopularHotels = adminDashboardService.getTopPopularHotels();
        model.addAttribute("topPopularHotels", topPopularHotels);

        List<Integer> checkInChartData = adminDashboardService.getCheckInChartData();
        model.addAttribute("checkInChartData", checkInChartData);
        List<Integer> checkOutChartData = adminDashboardService.getCheckOutChartData();
        model.addAttribute("checkOutChartData", checkOutChartData);

        return "page/adminDashboard"; // corresponds to templates/admin/adminDashboard.html
    }
}
