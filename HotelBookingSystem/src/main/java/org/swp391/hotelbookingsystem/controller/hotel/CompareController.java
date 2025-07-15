package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import java.util.List;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.model.Amenity;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.service.RoomService;

@Controller
public class CompareController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private AmenityService amenityService;
    @Autowired
    private RoomService roomService;

    @GetMapping("/hotel-compare")
    public String hotelComparePage(Model model) {
        List<Hotel> allHotels = hotelService.getAllHotels();
        model.addAttribute("allHotels", allHotels);
        return "page/hotelCompare";
    }

    // AJAX endpoint: trả về chi tiết khách sạn (phòng, amenities, ...)
    @GetMapping("/hotel-compare/detail/{hotelId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> getHotelCompareDetail(@org.springframework.web.bind.annotation.PathVariable Integer hotelId) {
        Map<String, Object> result = new HashMap<>();
        Hotel hotel = hotelService.getHotelById(hotelId);
        result.put("hotel", hotel);
        List<Room> rooms = roomService.getRoomByHotelId(hotelId);
        result.put("rooms", rooms);
        Set<String> amenities = new HashSet<>();
        for (Room room : rooms) {
            List<Amenity> roomAmenities = amenityService.getRoomAmenities(room.getRoomId());
            for (Amenity a : roomAmenities) {
                amenities.add(a.getName());
            }
        }
        result.put("amenities", amenities);
        return result;
    }
} 
