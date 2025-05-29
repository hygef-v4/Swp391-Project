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
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomTypeService;

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



    @PostMapping("/register-host")
    public String handleRegisterHost(
            @RequestParam("hotelName") String hotelName,
            @RequestParam("hotelAddress") String hotelAddress,
            @RequestParam("hotelLocationId") int locationId,
            @RequestParam("hotelImage") MultipartFile hotelImage,
            Model model
    ) {
        try {
            // Validate image type
            if (hotelImage.isEmpty() || hotelImage.getContentType() == null || !hotelImage.getContentType().startsWith("image/")) {
                model.addAttribute("error", "Chỉ được tải lên tệp ảnh (JPG, PNG, ...)");
                return "page/register-host";
            }

            // Upload image to Cloudinary
            Map result = cloudinaryService.uploadImage(hotelImage, "hotel-images");
            String imageUrl = (String) result.get("secure_url");

            //  Store imageUrl in database along with other hotel info
            // hotelService.saveHotel(..., imageUrl);

            // Optionally redirect or show success
            return "redirect:/host-dashboard"; // or whatever your success page is

        } catch (Exception e) {
            model.addAttribute("error", "Tải ảnh thất bại: " + e.getMessage());
            return "page/register-host";
        }
    }

}