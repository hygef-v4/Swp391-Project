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

    @Autowired
    UserService userService;

    @GetMapping("/register-host")
    public String showRegisterHostPage(Model model, HttpSession session) {
        session.setAttribute(ConstantVariables.ROOM_TYPES, roomTypeService.getAllRoomTypes());
        session.setAttribute(ConstantVariables.LOCATIONS, locationService.getAllLocations());

        //  Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();


        //  Group by category name instead of ID
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();  // use LinkedHashMap to maintain insertion order
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName();


//            if (!groupedAmenities.containsKey(categoryName)) {
//                groupedAmenities.put(categoryName, new ArrayList<>());
//            }
//            groupedAmenities.get(categoryName).add(amenity);

            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        return "page/register-host";
    }


    // đang để chung với register-host, có thể tách ra
    @GetMapping("/add-hotel")
    public String showAddListingPage(Model model, HttpSession session) {
        session.setAttribute(ConstantVariables.ROOM_TYPES, roomTypeService.getAllRoomTypes());
        session.setAttribute(ConstantVariables.LOCATIONS, locationService.getAllLocations());

        //  Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();


        //  Group by category name instead of ID
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName();

            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        return "page/register-host";
    }

    @GetMapping("/add-room")
    public String showAddRoomForm(@RequestParam("hotelId") int hotelId, Model model, HttpSession session) {
        User host = (User) session.getAttribute("user");
        if (host==null ||!host.getRole().equalsIgnoreCase("HOTEL OWNER")) {
            return "redirect:/login"; // Or handle unauthorized access
        }


        model.addAttribute("hotelId", hotelId);


        session.setAttribute(ConstantVariables.ROOM_TYPES, roomTypeService.getAllRoomTypes());
        session.setAttribute(ConstantVariables.LOCATIONS, locationService.getAllLocations());

        // Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();

        // Group amenities by category name
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName();
            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);

        return "host/host-add-room";
    }



    @GetMapping("/host-intro")
    public String showHostIntroPage() {
        return "page/host-intro";
    }


    @GetMapping("/host-dashboard")
    public String showHostDashboard(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");
        if (host==null ||!host.getRole().equalsIgnoreCase("HOTEL OWNER")) {
            return "redirect:/login"; // Or handle unauthorized access
        }

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("message", "Hotel registration successful! Welcome to your dashboard.");
        return "host/host-dashboard";
    }

    @GetMapping("/host-listing")
    public String viewHostListings(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        if (host==null ||!host.getRole().equalsIgnoreCase("HOTEL OWNER")) {
            return "redirect:/login"; // not logged in
        }

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);

        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);

        return "host/host-listing";
    }


    @PostMapping("/add-room")
    public String handleAddRoom(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("roomTypeId") int roomTypeId,
            @RequestParam("roomTitle") String roomTitle,
            @RequestParam("roomMaxGuests") int roomMaxGuests,
            @RequestParam("roomQuantity") int roomQuantity,
            @RequestParam("roomPrice") float roomPrice,
            @RequestParam("roomDescription") String roomDescription,
            @RequestParam(value = "amenities", required = false) List<Integer> amenityIds,
            @RequestParam("roomImageFiles") MultipartFile[] roomImageFiles,    //MultipartFile = Spring's file upload object
            HttpSession session,
            Model model
    ) {
        try {
            // Upload room images
            List<String> roomImageUrls = new ArrayList<>();
            if (roomImageFiles != null) {
                for (MultipartFile file : roomImageFiles) {
                    if (!file.isEmpty() && file.getContentType().startsWith("image/")) {
                        String url = (String) cloudinaryService
                                .uploadImage(file, "room-images/" + hotelId)
                                .get("secure_url");
                        roomImageUrls.add(url);
                    }
                }
            }

            // Build and save room
            Room room = Room.builder()
                    .hotelId(hotelId)
                    .title(roomTitle)
                    .description(roomDescription)
                    .roomTypeId(roomTypeId)
                    .maxGuests(roomMaxGuests)
                    .quantity(roomQuantity)
                    .price(roomPrice)
                    .status("active")
                    .build();

            roomService.saveRoom(room, amenityIds, roomImageUrls);

            return "redirect:/host-listing"; // or /host-dashboard if preferred
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi thêm phòng: " + e.getMessage());
            return "host/host-add-room"; // fallback to the same page on error
        }
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
            @RequestParam("roomPrice") float roomPrice,
            @RequestParam("roomDescription") String roomDescription,
            @RequestParam(value = "amenities", required = false) List<Integer> amenityIds,

            @RequestParam("roomImageFiles") MultipartFile[] roomImageFiles,

            HttpSession session,
            Model model
    ) {
        try {
            User user = (User) session.getAttribute("user");
            int userId = user.getId();

            if (!"HOTEL OWNER".equalsIgnoreCase(user.getRole())) {
                user.setRole("HOTEL OWNER");
                userService.updateUserRoleToHost(userId);
                session.setAttribute("user", user); // cập nhật session
            }


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