package org.swp391.hotelbookingsystem.controller.user;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.AmenityCategory;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {
    @Autowired
    LocationService locationService;
    @Autowired
    HotelService hotelService;
    @Autowired
    RoomService roomService;
    @Autowired
    AmenityService amenityService;

    @GetMapping("/booking/{id}")
    public String booking(
        @PathVariable(value = "id") int hotelId,
        @RequestParam(value = "dateRange") String dateRange,
        
        Model model, HttpSession session
    ){
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        Hotel hotel = hotelService.getHotelById(hotelId);
        hotel.setPolicy(hotel.getPolicy().replace("<li>", "<li class=\"list-group-item d-flex\"><i class=\"bi bi-arrow-right me-2\"></i>"));
        model.addAttribute("hotel", hotel);
        
        List<Room> rooms = roomService.getAvailableRoomsByHotelId(hotelId);
        for(Room room : rooms){
            room.setDescription("<li>Sức chứa: " + room.getMaxGuests() + " Người</li>" + room.getDescription());

            List<Amenity> amenities = amenityService.getRoomAmenities(room.getRoomId());
            List<AmenityCategory> categories = new ArrayList<>();
            String category = "";
            int index = -1;

            for(Amenity amenity : amenities){
                if(!category.equals(amenity.getCategory().getName())){
                    category = amenity.getCategory().getName();
                    categories.add(new AmenityCategory(amenity.getAmenityId(), category, new ArrayList<>()));
                    index++;
                }categories.get(index).getAmenities().add(amenity);
            }

            room.setCategories(categories);
        }model.addAttribute("rooms", rooms);

        String hotelName = hotel.getHotelName();
        String encode = URLEncoder.encode(hotelName, StandardCharsets.UTF_8);
        String map = "https://www.google.com/maps/search/" + encode + "//@" + hotel.getLatitude() + "," + hotel.getLongitude() + ",17z";
        model.addAttribute("map", map);

        String[] dates = dateRange.split(" => ");
        model.addAttribute("checkIn", dates[0]);
        model.addAttribute("checkOut", dates.length > 1 ? dates[1] : dates[0]);

        return "page/booking";
    }
}
