package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.Amenity;
import java.util.*;
import java.math.BigDecimal;

@Controller
public class CompareController {
    @GetMapping("/hotel-compare")
    public String hotelCompareDemo(Model model) {
        // Dữ liệu mẫu cho 3 khách sạn
        List<Hotel> hotels = new ArrayList<>();
        Map<Integer, List<Room>> roomsMap = new HashMap<>();
        Map<Integer, List<String>> amenitiesMap = new HashMap<>();
        Map<Integer, Integer> roomTypeCountMap = new HashMap<>();
        Map<Integer, Integer> totalRoomCountMap = new HashMap<>();
        Map<Integer, Integer> amenityCountMap = new HashMap<>();
        Map<Integer, Integer> maxGuestsMap = new HashMap<>();
        Map<Integer, List<Map<String, Object>>> reviewsMap = new HashMap<>();

        for (int i = 1; i <= 3; i++) {
            Hotel h = new Hotel();
            h.setHotelId(i);
            h.setHotelName("Khách sạn Demo " + i);
            h.setHotelImageUrl("/assets/images/category/hotel/0" + i + ".jpg");
            h.setRating(4.0 + i * 0.3);
            h.setAddress("Số " + (10 + i) + " Đường Demo");
            h.setCityName(i == 1 ? "Hà Nội" : (i == 2 ? "Đà Nẵng" : "Hồ Chí Minh"));
            h.setMinPrice(BigDecimal.valueOf(500000 + i * 200000));
            h.setRoomQuantity(i == 2 ? 0 : 10); // Khách sạn 2 không có roomQuantity để test
            hotels.add(h);

            // Dữ liệu mẫu cho Room
            List<Room> rooms = new ArrayList<>();
            if (i != 3) { // Khách sạn 3 không có phòng để test
                for (int j = 1; j <= 2; j++) {
                    Room r = new Room();
                    r.setRoomId(j);
                    r.setHotelId(i);
                    r.setTitle("Phòng loại " + j);
                    r.setQuantity(5);
                    r.setMaxGuests(3 + j * i);
                    List<Amenity> ams = new ArrayList<>();
                    for (int k = 1; k <= 2; k++) {
                        Amenity a = new Amenity();
                        a.setAmenityId(k);
                        a.setName("Tiện nghi " + (j * k));
                        ams.add(a);
                    }
                    r.setAmenities(ams);
                    rooms.add(r);
                }
            }
            roomsMap.put(i, rooms);

            // Số loại phòng
            roomTypeCountMap.put(i, rooms.size());

            // Tổng số phòng
            int totalRoom = 0;
            if (h.getRoomQuantity() > 0) {
                totalRoom = h.getRoomQuantity();
            } else if (!rooms.isEmpty()) {
                for (Room r : rooms) totalRoom += r.getQuantity();
            }
            totalRoomCountMap.put(i, totalRoom);

            // Số lượng tiện nghi (không trùng lặp)
            Set<String> amenitySet = new HashSet<>();
            for (Room r : rooms) {
                if (r.getAmenities() != null) {
                    for (Amenity a : r.getAmenities()) {
                        amenitySet.add(a.getName());
                    }
                }
            }
            amenitiesMap.put(i, new ArrayList<>(amenitySet));
            amenityCountMap.put(i, amenitySet.size());

            // Số khách tối đa/phòng
            int maxGuests = 0;
            for (Room r : rooms) {
                if (r.getMaxGuests() > maxGuests) maxGuests = r.getMaxGuests();
            }
            maxGuestsMap.put(i, maxGuests);

            // Dữ liệu review mẫu cho từng khách sạn
            List<Map<String, Object>> reviews = new ArrayList<>();
            for (int j = 1; j <= 2; j++) {
                Map<String, Object> review = new HashMap<>();
                review.put("fullName", "Người dùng " + j);
                review.put("avatarUrl", "/assets/images/avatar/0" + j + ".jpg");
                review.put("rating", 4 + (j % 2));
                review.put("comment", "Khách sạn rất tuyệt vời! Tôi sẽ quay lại lần nữa. (" + h.getHotelName() + ")");
                reviews.add(review);
            }
            reviewsMap.put(i, reviews);
        }

        // Nếu không có dữ liệu thì truyền "Không có thông tin" hoặc 0
        for (Hotel h : hotels) {
            int id = h.getHotelId();
            if (!roomTypeCountMap.containsKey(id)) roomTypeCountMap.put(id, 0);
            if (!totalRoomCountMap.containsKey(id)) totalRoomCountMap.put(id, 0);
            if (!amenityCountMap.containsKey(id)) amenityCountMap.put(id, 0);
            if (!maxGuestsMap.containsKey(id)) maxGuestsMap.put(id, 0);
            if (!amenitiesMap.containsKey(id)) amenitiesMap.put(id, new ArrayList<>());
        }

        model.addAttribute("hotels", hotels);
        model.addAttribute("amenitiesMap", amenitiesMap);
        model.addAttribute("roomTypeCountMap", roomTypeCountMap);
        model.addAttribute("totalRoomCountMap", totalRoomCountMap);
        model.addAttribute("amenityCountMap", amenityCountMap);
        model.addAttribute("maxGuestsMap", maxGuestsMap);
        model.addAttribute("reviewsMap", reviewsMap);
        return "page/hotelCompare";
    }
} 
