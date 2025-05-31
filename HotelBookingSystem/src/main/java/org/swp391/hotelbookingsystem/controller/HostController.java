package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HostController {

    @Autowired
    RoomTypeService roomTypeService;

    @Autowired
    LocationService locationService;

    @Autowired
    AmenityService amenityService;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    RoomService roomService;
    @Autowired
    HotelService hotelService;

    @GetMapping("/register-host")
    public String showRegisterHostPage(Model model, HttpSession session) {
        session.setAttribute(ConstantVariables.ROOM_TYPES, roomTypeService.getAllRoomTypes());
        session.setAttribute(ConstantVariables.LOCATIONS, locationService.getAllLocations());

        //  Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();


        //  Group by category name instead of ID
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName(); // assumes Amenity has getCategory().getName()

            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        return "page/register-host";
    }


    @GetMapping("/add-listing")
    public String showAddListingPage(Model model) {
        return "page/add-listing";
    }


    // You might also have a GET mapping for the host-intro page
    @GetMapping("/host-intro")
    public String showHostIntroPage() {
        return "page/host-intro";
    }

    // A simple host dashboard placeholder
    @GetMapping("/host-dashboard")
    public String showHostDashboard(Model model) {
        model.addAttribute("message", "Hotel registration successful! Welcome to your dashboard.");
        return "host/host-dashboard"; // Create this Thymeleaf template
    }

    @PostMapping("/register-host")
    public String handleRegisterHost(
            @RequestParam("hotelName") String hotelName,
            @RequestParam("hotelDescription") String hotelDescription,
            @RequestParam("hotelImage") MultipartFile hotelImage,
            @RequestParam("hotelAddress") String hotelAddress,
            @RequestParam("hotelLocationId") int locationId,
            @RequestParam("hotelLatitude") String latitudeStr,
            @RequestParam("hotelLongitude") String longitudeStr,
            @RequestParam("hotelPolicies") String hotelPolicies,

            @RequestParam("roomTypeId") int roomTypeId,
            @RequestParam("roomTitle") String roomTitle,
            @RequestParam("roomMaxGuests") int roomMaxGuests,
            @RequestParam("roomQuantity") int roomQuantity,
            @RequestParam("roomPrice") BigDecimal roomPrice,
            @RequestParam("roomDescription") String roomDescription,
            @RequestParam(value = "amenities", required = false) List<Integer> amenityIds,

            @RequestParam("roomImageFiles") MultipartFile[] roomImageFiles,

            HttpSession session,
            Model model
    ) {
        try {
            User user = (User) session.getAttribute("user");
            int userId =   user.getId();

            // Validate & upload hotel image
            if (hotelImage.isEmpty() || hotelImage.getContentType() == null || !hotelImage.getContentType().startsWith("image/")) {
                model.addAttribute("error", "Chỉ được tải lên tệp ảnh (JPG, PNG, ...)");
                return "page/register-host";
            }
            String hotelImageUrl = (String) cloudinaryService.uploadImage(hotelImage, "hotel-main-images").get("secure_url");
            // Parse latitude/longitude
            BigDecimal latitude = new BigDecimal(latitudeStr);
            BigDecimal longitude = new BigDecimal(longitudeStr);

            //  hotel
            Hotel hotel = Hotel.builder()
                    .hostId(userId)
                    .hotelName(hotelName)
                    .description(hotelDescription)
                    .address(hotelAddress)
                    .locationId(locationId)
                    .latitude(latitude)
                    .longitude(longitude)
                    .hotelImageUrl(hotelImageUrl)
                    .policy(hotelPolicies)
                    .build(); // no .rating() // because it's optional


            Hotel savedHotel = hotelService.saveHotel(hotel);

            // Upload room images
            List<String> roomImageUrls = new ArrayList<>();
            if (roomImageFiles != null) {
                for (MultipartFile file : roomImageFiles) {
                    if (!file.isEmpty() && file.getContentType().startsWith("image/")) {
                        String url = (String) cloudinaryService.uploadImage(file, "room-images/" + savedHotel.getHotelId()).get("secure_url");
                        roomImageUrls.add(url);
                    }
                }
            }


            // room
            Room room = Room.builder()
                    .hotelId(savedHotel.getHotelId())
                    .title(roomTitle)
                    .description(roomDescription)
                    .roomTypeId(roomTypeId)
                    .maxGuests(roomMaxGuests)
                    .quantity(roomQuantity)
                    .price(roomPrice)
                    .status("active")
                    .build();

            roomService.saveRoom(room, amenityIds, roomImageUrls);
            return "redirect:/host-dashboard"; // Redirect to the host dashboard after successful registration
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi khi tạo khách sạn: " + e.getMessage());
            return "page/register-host";
        }
    }

}