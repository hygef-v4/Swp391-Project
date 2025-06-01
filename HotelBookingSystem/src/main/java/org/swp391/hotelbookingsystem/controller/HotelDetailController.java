package org.swp391.hotelbookingsystem.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomService;

@Controller
public class HotelDetailController {
    @Autowired
    LocationService locationService;
    @Autowired
    HotelService hotelService;
    @Autowired
    RoomService roomService;

    @GetMapping("/hotel-detail")
    public String hotelDetail(@RequestParam(value = "hotelId") int hotelId, Model model){
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel.getPolicy() != null) {
            hotel.setPolicy(hotel.getPolicy().replace(
                    "<li>",
                    "<li class=\"list-group-item d-flex\"><i class=\\\"bi bi-arrow-right me-2\\\"></i>"
            ));
        } else {
            hotel.setPolicy("");
        }
        model.addAttribute("hotel", hotel);


        int index = hotel.getDescription().indexOf("<br><br><b>");
        if(index != -1){
            model.addAttribute("short", hotel.getDescription().substring(0, index));
            model.addAttribute("long", hotel.getDescription().substring(index));
        }else{
            if(hotel.getDescription().length() > 800){
                model.addAttribute("short", hotel.getDescription().substring(0, 800));
                model.addAttribute("long", hotel.getDescription().substring(800));                
            }else{
                model.addAttribute("short", hotel.getDescription());
                model.addAttribute("long", "");
            }
        }

        List<Room> rooms = roomService.getRoomByHotelId(hotelId);
        model.addAttribute("rooms", rooms);
        
        String hotelName = hotel.getHotelName();
        String encode = URLEncoder.encode(hotelName, StandardCharsets.UTF_8);
        String map = "https://www.google.com/maps/search/" + encode + "//@" + hotel.getLatitude() + "," + hotel.getLongitude() + ",17z";
        model.addAttribute("map", map);

        return "page/hotelDetail";
    }    
}
