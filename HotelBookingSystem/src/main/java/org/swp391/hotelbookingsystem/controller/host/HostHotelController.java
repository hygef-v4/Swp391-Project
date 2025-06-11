package org.swp391.hotelbookingsystem.controller.host;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HostHotelController {

    final
    RoomTypeService roomTypeService;

    final
    LocationService locationService;

    final
    AmenityService amenityService;

    final
    CloudinaryService cloudinaryService;

    final
    RoomService roomService;
    final
    HotelService hotelService;

    final
    UserService userService;

    public HostHotelController(RoomTypeService roomTypeService, LocationService locationService, AmenityService amenityService, CloudinaryService cloudinaryService, RoomService roomService, HotelService hotelService, UserService userService) {
        this.roomTypeService = roomTypeService;
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.cloudinaryService = cloudinaryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.userService = userService;
    }

    @GetMapping("/host-listing")
    public String viewHostListings(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login"; // not logged in
        }

        int totalRooms = roomService.getTotalRoomsByHostId(host.getId());
        model.addAttribute("totalRooms", totalRooms);

        List<Hotel> hotels = hotelService.getHotelsByHostId(host.getId());
        model.addAttribute("hotels", hotels);

        return "host/host-listing";
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

            groupedAmenities.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        return "page/register-host";
    }


    @GetMapping("/add-room")
    public String showAddRoomForm(@RequestParam("hotelId") int hotelId, Model model, HttpSession session) {
        User host = (User) session.getAttribute("user");
        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login"; // Or handle unauthorized access
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != host.getId()) {
            return "redirect:/host-listing"; // prevent unauthorized access
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

    @GetMapping("/manage-hotel")
    public String showManageHotelPage(@RequestParam("hotelId") int hotelId, Model model, HttpSession session) {
        User host = (User) session.getAttribute("user");
        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null || hotel.getHostId() != host.getId()) {
            return "redirect:/host-listing";
        }

        return "host/host-manage-hotel";
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


}
