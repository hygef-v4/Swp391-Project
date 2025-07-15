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
import org.swp391.hotelbookingsystem.service.ReviewService;

@Controller
public class CompareController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private AmenityService amenityService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ReviewService reviewService;

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
        // Thêm số lượng phòng còn lại
        int availableRooms = roomService.countAvailableRoomsByHotelId(hotelId);
        result.put("availableRooms", availableRooms);
        // Thêm review nổi bật
        org.swp391.hotelbookingsystem.model.Review featuredReview = null;
        List<org.swp391.hotelbookingsystem.model.Review> reviews = reviewService.getHotelReview(hotelId);
        if (reviews != null && !reviews.isEmpty()) {
            // Lấy review có rating cao nhất, nếu nhiều review cùng rating thì lấy review mới nhất
            featuredReview = reviews.stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getRating(), a.getRating());
                    if (cmp == 0) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                })
                .findFirst().orElse(null);
        }
        result.put("featuredReview", featuredReview);
        return result;
    }
} 
