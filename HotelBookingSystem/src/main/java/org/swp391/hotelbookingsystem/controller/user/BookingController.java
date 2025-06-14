package org.swp391.hotelbookingsystem.controller.user;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.AmenityCategory;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
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

    // @PostMapping("/booking")
    // public String addBooking(@RequestParam("hotelId") int hotelId, @RequestParam("roomId") int roomId, @RequestParam("customerId") int customerId){
    //     return "Booking added successfully for hotel ID: " + hotelId + ", room ID: " + roomId + ", customer ID: " + customerId;
    // }

    @GetMapping("/booking")
    public String booking(
        @RequestParam(value = "hotelId", defaultValue = "1") int hotelId,
    
        @RequestParam(value = "adults", defaultValue = "1") int adults,
        @RequestParam(value = "children", defaultValue = "0") int children,
        @RequestParam(value = "rooms", defaultValue = "1") int roomQuantity,
        
        Model model, HttpSession session
    ){
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
        model.addAttribute("roomQuantity", roomQuantity);

        Hotel hotel = hotelService.getHotelById(hotelId);
        hotel.setPolicy(hotel.getPolicy().replace("<li>", "<li class=\"list-group-item d-flex\"><i class=\"bi bi-arrow-right me-2\"></i>"));
        model.addAttribute("hotel", hotel);

        User user = (User) session.getAttribute("user");
        boolean favorite = false;
        if (user != null) {
            favorite = hotelService.isFavoriteHotel(user.getId(), hotelId);
        }model.addAttribute("favorite", favorite);

        String redirect = "";
        if(adults != 1) redirect += "&adults=" + adults;
        if(children != 0) redirect += "&children=" + children;
        if(roomQuantity != 1) redirect += "&rooms=" + roomQuantity;
        model.addAttribute("redirect", redirect);

        String description = hotel.getDescription();
        if (description != null) {
            int index = description.indexOf("<br><br><b>");
            if (index != -1) {
                model.addAttribute("short", description.substring(0, index));
                model.addAttribute("long", description.substring(index));
            } else {
                if (description.length() > 800) {
                    model.addAttribute("short", description.substring(0, 800));
                    model.addAttribute("long", description.substring(800));
                } else {
                    model.addAttribute("short", description);
                    model.addAttribute("long", "");
                }
            }
        }
        
        model.addAttribute("hotel", hotel);

        List<Room> rooms = roomService.getRoomByHotelId(hotelId);
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

        return "page/booking";
    }
}
