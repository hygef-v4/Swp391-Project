package org.swp391.hotelbookingsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;

@Controller
public class HotelController {
    @Autowired
    HotelService hotelService;

    @Autowired
    LocationService locationService;

    @GetMapping("/hotel-list")
    public String hotelList(@RequestParam(value = "locationId", defaultValue = "-1") int locationId, @RequestParam(value = "search", defaultValue = "") String search, Model model){
        List<Location> location = locationService.getLocationById(locationId);
        model.addAttribute("hamora", locationId == -1 ? new Location(locationId, "Hamora", "assets/images/bg/05.jpg") : location.get(0));
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        List<Hotel> hotel = hotelService.getHotelsByLocation(locationId, search);
        model.addAttribute("hotels", hotel);

        String request = "";
        if(locationId != -1) request += "&locationId=" + locationId;
        model.addAttribute("request", request);

        return "page/hotelList";
    }
}
