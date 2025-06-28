package org.swp391.hotelbookingsystem.controller.hotel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.AmenityCategory;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.RoomService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HotelDetailController {
    @Autowired
    LocationService locationService;
    @Autowired
    HotelService hotelService;
    @Autowired
    RoomService roomService;
    @Autowired
    AmenityService amenityService;
    @Autowired
    ReviewService reviewService;

    @GetMapping("/hotel-detail")
    public String hotelDetail(
        @RequestParam(value = "hotelId") int hotelId,
    
        @RequestParam(value = "dateRange", defaultValue = "") String dateRange,
        @RequestParam(value = "guests", defaultValue = "1") int guests,
        @RequestParam(value = "rooms", defaultValue = "1") int roomQuantity,
        
        Model model, HttpSession session
    ){
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("dateRange", dateRange);
        String[] date = dateRange.split(" => ");
        Date checkin = !date[0].isBlank() ? Date.valueOf(date[0]) : null;
        Date checkout = date.length > 1 ? Date.valueOf(date[1]) : checkin;

        model.addAttribute("guests", guests);
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
        if(!"".equals(dateRange)) redirect += "&dateRange=" + URLEncoder.encode(dateRange, StandardCharsets.UTF_8);
        if(guests != 1) redirect += "&guests=" + guests;
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
        
        List<Room> rooms = roomService.getRoomsByIdAndDateRange(hotelId, checkin, checkout);
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

        if(user != null){
            boolean commented = reviewService.checkReview(hotelId, user.getId());
            model.addAttribute("comment", commented);
        }

        List<Review> comments = reviewService.getHotelReview(hotelId);
        model.addAttribute("comments", comments);

        String hotelName = hotel.getHotelName();
        String encode = URLEncoder.encode(hotelName, StandardCharsets.UTF_8);
        String map = "https://www.google.com/maps/search/" + encode + "//@" + hotel.getLatitude() + "," + hotel.getLongitude() + ",17z";
        model.addAttribute("map", map);

        return "page/hotelDetail";
    }    
}
