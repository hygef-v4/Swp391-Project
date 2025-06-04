package org.swp391.hotelbookingsystem.controller.hotel;

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
public class HotelListController {
    @Autowired
    HotelService hotelService;
    @Autowired
    LocationService locationService;

    @GetMapping("/hotel-list")
    public String hotelList(
        @RequestParam(value = "locationId", defaultValue = "-1") int locationId,
        @RequestParam(value = "adults", defaultValue = "1") int adults, 
        @RequestParam(value = "children", defaultValue = "0") int children, 
        @RequestParam(value = "rooms", defaultValue = "1") int rooms, 
        @RequestParam(value = "search", defaultValue = "") String search, 
        @RequestParam(value = "page", defaultValue = "1") int page, Model model
    ){
        List<Location> location = locationService.getLocationById(locationId);
        model.addAttribute("hamora", locationId == -1 ? new Location(locationId, "Hamora", "assets/images/bg/05.jpg") : location.get(0));
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("search", search.trim());


        List<Hotel> hotel = hotelService.getHotelsByLocation(locationId, (adults + children), rooms, search.trim());
        int item = page * 12;
        
        List<Hotel> current;
        if(hotel.size() < item){
            current = hotel.subList((page-1) * 12, hotel.size());
        }else current = hotel.subList((page-1) * 12, item);
        model.addAttribute("hotels", current);

        model.addAttribute("page", page);
        model.addAttribute("pagination", (int)Math.ceil((double)hotel.size() / 12));

        String request = "";
        if(locationId != -1) request += "&locationId=" + locationId;
        if(adults != 1) request += "&adults=" + adults;
        if(children != 0) request += "&children=" + children;
        if(rooms != 1) request += "&rooms=" + rooms;
        model.addAttribute("request", request);

        return "page/hotelList";
    }
}
