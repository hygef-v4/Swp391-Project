package org.swp391.hotelbookingsystem.controller.host;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.RoomTypes;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.RoomTypeService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;

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

        // Get hotel details and rooms
        List<Room> rooms = roomService.getRoomByHotelId(hotelId);
        
        // Populate room type names and amenities for each room
        for (Room room : rooms) {
            // Get room type name
            if (room.getRoomTypeId() > 0) {
                try {
                    RoomTypes roomType = roomTypeService.getRoomTypeById(room.getRoomTypeId());
                    if (roomType != null) {
                        room.setRoomType(roomType.getName());
                    }
                } catch (Exception e) {
                    room.setRoomType("Unknown");
                }
            }
            
            // Get room amenities
            List<Amenity> roomAmenities = amenityService.getRoomAmenities(room.getRoomId());
            room.setAmenities(roomAmenities);
        }
        
        // Get available amenities, room types, and locations for editing
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName();
            groupedAmenities.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(amenity);
        }
        
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);
        model.addAttribute("groupedAmenities", groupedAmenities);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("locations", locationService.getAllLocations());

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

    @PostMapping("/update-hotel")
    @ResponseBody
    public Map<String, Object> updateHotel(
            @RequestParam("hotelId") int hotelId,
            @RequestParam(value = "hotelName", required = false) String hotelName,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "locationId", required = false) Integer locationId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "policy", required = false) String policy,
            @RequestParam(value = "latitude", required = false) String latitude,
            @RequestParam(value = "longitude", required = false) String longitude,
            @RequestParam(value = "hotelImage", required = false) MultipartFile hotelImage,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("success", false);
                response.put("message", "Không có quyền truy cập");
                return response;
            }

            Hotel hotel = hotelService.getHotelById(hotelId);
            if (hotel == null || hotel.getHostId() != host.getId()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy khách sạn hoặc không có quyền chỉnh sửa");
                return response;
            }

            // Update hotel image if provided
            String hotelImageUrl = hotel.getHotelImageUrl();
            if (hotelImage != null && !hotelImage.isEmpty()) {
                try {
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(hotelImage, "hotel-main-images");
                    hotelImageUrl = (String) uploadResult.get("secure_url");
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Lỗi khi tải lên hình ảnh: " + e.getMessage());
                    return response;
                }
            }

            // Parse latitude and longitude if provided
            BigDecimal updatedLatitude = hotel.getLatitude();
            BigDecimal updatedLongitude = hotel.getLongitude();
            
            if (latitude != null && !latitude.trim().isEmpty()) {
                try {
                    updatedLatitude = new BigDecimal(latitude.trim());
                } catch (NumberFormatException e) {
                    response.put("success", false);
                    response.put("message", "Vĩ độ không hợp lệ");
                    return response;
                }
            }
            
            if (longitude != null && !longitude.trim().isEmpty()) {
                try {
                    updatedLongitude = new BigDecimal(longitude.trim());
                } catch (NumberFormatException e) {
                    response.put("success", false);
                    response.put("message", "Kinh độ không hợp lệ");
                    return response;
                }
            }

            // Build updated hotel object
            Hotel updatedHotel = Hotel.builder()
                    .hotelId(hotel.getHotelId())
                    .hostId(hotel.getHostId())
                    .hotelName(hotelName != null ? hotelName : hotel.getHotelName())
                    .address(address != null ? address : hotel.getAddress())
                    .locationId(locationId != null ? locationId : hotel.getLocationId())
                    .description(description != null ? description : hotel.getDescription())
                    .policy(policy != null ? policy : hotel.getPolicy())
                    .hotelImageUrl(hotelImageUrl)
                    .latitude(updatedLatitude)
                    .longitude(updatedLongitude)
                    .rating(hotel.getRating())
                    .minPrice(hotel.getMinPrice())
                    .build();

            hotelService.updateHotel(updatedHotel);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin khách sạn thành công");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/update-room")
    @ResponseBody
    public Map<String, Object> updateRoom(
            @RequestParam("roomId") int roomId,
            @RequestParam("hotelId") int hotelId,
            @RequestParam("roomTitle") String roomTitle,
            @RequestParam("roomTypeId") int roomTypeId,
            @RequestParam("maxGuests") int maxGuests,
            @RequestParam("quantity") int quantity,
            @RequestParam("price") float price,
            @RequestParam("description") String description,
            @RequestParam(value = "amenities", required = false) List<Integer> amenityIds,
            @RequestParam(value = "roomImages", required = false) MultipartFile[] roomImages,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("success", false);
                response.put("message", "Không có quyền truy cập");
                return response;
            }

            Hotel hotel = hotelService.getHotelById(hotelId);
            if (hotel == null || hotel.getHostId() != host.getId()) {
                response.put("success", false);
                response.put("message", "Không có quyền chỉnh sửa phòng này");
                return response;
            }

            // Upload new room images if provided
            List<String> roomImageUrls = new ArrayList<>();
            if (roomImages != null && roomImages.length > 0) {
                for (MultipartFile file : roomImages) {
                    if (!file.isEmpty() && file.getContentType().startsWith("image/")) {
                        String url = (String) cloudinaryService
                                .uploadImage(file, "room-images/" + hotelId)
                                .get("secure_url");
                        roomImageUrls.add(url);
                    }
                }
            }

            // Update room
            Room updatedRoom = Room.builder()
                    .roomId(roomId)
                    .hotelId(hotelId)
                    .title(roomTitle)
                    .description(description)
                    .roomTypeId(roomTypeId)
                    .maxGuests(maxGuests)
                    .quantity(quantity)
                    .price(price)
                    .status("active")
                    .build();

            roomService.updateRoom(updatedRoom, amenityIds, roomImageUrls);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin phòng thành công");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật phòng: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/delete-room")
    @ResponseBody
    public Map<String, Object> deleteRoom(
            @RequestParam("roomId") int roomId,
            @RequestParam("hotelId") int hotelId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Authentication and authorization check
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("success", false);
                response.put("message", "Không có quyền truy cập");
                return response;
            }

            // 2. Hotel ownership verification
            Hotel hotel;
            try {
                hotel = hotelService.getHotelById(hotelId);
                if (hotel == null || hotel.getHostId() != host.getId()) {
                    response.put("success", false);
                    response.put("message", "Không có quyền xóa phòng này");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error fetching hotel data for hotelId " + hotelId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra thông tin khách sạn");
                return response;
            }

            // 3. Check if this is the last room in the hotel
            List<Room> currentRooms;
            try {
                currentRooms = roomService.getRoomsByHotelId(hotelId);
                if (currentRooms.size() <= 1) {
                    response.put("success", false);
                    response.put("message", "Không thể xóa phòng cuối cùng. Khách sạn phải có ít nhất 1 phòng.");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error fetching rooms for hotelId " + hotelId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra danh sách phòng");
                return response;
            }

            // 4. Check if the room has active booking units (approved)
            try {
                if (roomService.hasActiveBookingUnits(roomId)) {
                    response.put("success", false);
                    response.put("message", "Không thể xóa phòng này vì có khách đang đặt phòng.");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error checking active bookings for roomId " + roomId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra booking đang hoạt động");
                return response;
            }

            // 5. Actually delete the room
            try {
                roomService.deleteRoom(roomId);
                System.out.println("Successfully deleted room " + roomId);
            } catch (Exception e) {
                System.err.println("Error deleting room " + roomId + ": " + e.getMessage());
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Lỗi khi xóa phòng: " + e.getMessage());
                return response;
            }

            // 6. Success response
            response.put("success", true);
            response.put("message", "Xóa phòng thành công");
            
        } catch (Exception e) {
            // Catch any unexpected errors
            System.err.println("Unexpected error in deleteRoom for roomId " + roomId + ": " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi không mong muốn khi xóa phòng");
        }
        
        return response;
    }


}
