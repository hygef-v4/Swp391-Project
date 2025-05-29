package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomTypeService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HostController {

    @Autowired
    RoomTypeService roomTypeService;

    @Autowired
    LocationService locationService;

    @Autowired
    AmenityService amenityService;

    @GetMapping("/register-host")
    public String showRegisterHostPage(Model model, HttpSession session) {
        session.setAttribute(ConstantVariables.ROOM_TYPES, roomTypeService.getAllRoomTypes());
        session.setAttribute(ConstantVariables.LOCATIONS, locationService.getAllLocations());

        //  Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();


        //  Group by category name instead of ID
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName(); // assumes Amenity has getCategory().getName()

            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        return "page/register-host";
    }


    @GetMapping("/add-listing")
    public String showAddListingPage(Model model) {
        return "page/add-listing";
    }


    // You might also have a GET mapping for the host-intro page
    @GetMapping("/host-intro")
    public String showHostIntroPage() {
        return "page/host-intro";
    }


}