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
    public String hotelCompareDemo(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        if (hotels.size() > 3) {
            hotels = hotels.subList(0, 3);
        }
        model.addAttribute("hotels", hotels);

        // Lấy danh sách phòng cho từng khách sạn
        Map<Integer, List<Room>> hotelRoomsMap = new HashMap<>();
        // Map: hotelId -> Set<Amenity>
        Map<Integer, Set<String>> hotelAmenitiesMap = new HashMap<>();
        // Tổng hợp tất cả amenities xuất hiện ở bất kỳ khách sạn nào
        Set<String> allAmenities = new HashSet<>();
        for (Hotel hotel : hotels) {
            List<Room> rooms = roomService.getRoomByHotelId(hotel.getHotelId());
            hotelRoomsMap.put(hotel.getHotelId(), rooms);
            Set<String> amenitiesOfHotel = new HashSet<>();
            for (Room room : rooms) {
                List<Amenity> amenities = amenityService.getRoomAmenities(room.getRoomId());
                for (Amenity a : amenities) {
                    amenitiesOfHotel.add(a.getName());
                    allAmenities.add(a.getName());
                }
            }
            hotelAmenitiesMap.put(hotel.getHotelId(), amenitiesOfHotel);
        }
        model.addAttribute("hotelRoomsMap", hotelRoomsMap);
        model.addAttribute("hotelAmenitiesMap", hotelAmenitiesMap);
        model.addAttribute("allAmenities", allAmenities);
        return "page/hotelCompare";
    }
} 
