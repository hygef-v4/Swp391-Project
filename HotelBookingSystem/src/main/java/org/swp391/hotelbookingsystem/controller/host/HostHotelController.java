package org.swp391.hotelbookingsystem.controller.host;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.CancellationPolicy;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.CancellationPolicyService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HostHotelController {

    @Value("${app.base-url}")
    private String baseUrl;

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

    final 
    NotificationService notificationService;

    final 
    CancellationPolicyService cancellationPolicyService;

    final 
    BookingService bookingService;

    public HostHotelController(LocationService locationService, AmenityService amenityService, CloudinaryService cloudinaryService, RoomService roomService, HotelService hotelService, UserService userService, CancellationPolicyService cancellationPolicyService, NotificationService notificationService, BookingService bookingService) {
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.cloudinaryService = cloudinaryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.cancellationPolicyService = cancellationPolicyService;
        this.notificationService = notificationService;
        this.bookingService = bookingService;
    }

    @GetMapping("/host-listing")
    public String viewHostListings(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model) {
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            return "redirect:/login"; // not logged in
        }

        // Pagination settings
        int pageSize = 6; // Number of hotels per page
        int offset = (page - 1) * pageSize;

        // Get hotels with search and pagination
        List<Hotel> hotels;
        int totalHotels;

        if (search != null && !search.trim().isEmpty()) {
            hotels = hotelService.getHotelsByHostIdWithSearchAndPagination(host.getId(), search, offset, pageSize);
            totalHotels = hotelService.countHotelsByHostIdAndSearch(host.getId(), search);
        } else {
            hotels = hotelService.getHotelsByHostIdWithSearchAndPagination(host.getId(), "", offset, pageSize);
            totalHotels = hotelService.countHotelsByHostIdAndSearch(host.getId(), "");
        }

        // Calculate pagination data
        int totalPages = (int) Math.ceil((double) totalHotels / pageSize);

        model.addAttribute("hotels", hotels);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalHotels", totalHotels);
        model.addAttribute("search", search);
        model.addAttribute("pageSize", pageSize);

        return "host/host-listing";
    }

    // đang để chung với register-host, có thể tách ra
    @GetMapping("/add-hotel")
    public String showAddListingPage(Model model, HttpSession session) {
        session.setAttribute("locations", locationService.getAllLocations());

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

        session.setAttribute("locations", locationService.getAllLocations());

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
        
        // Populate amenities for each room
        for (Room room : rooms) {
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

        // Get cancellation policy for the hotel
        CancellationPolicy cancellationPolicy = cancellationPolicyService.getCancellationPolicyByHotelId(hotelId);
        
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);
        model.addAttribute("groupedAmenities", groupedAmenities);
        model.addAttribute("locations", locationService.getAllLocations());
        model.addAttribute("cancellationPolicy", cancellationPolicy);

        return "host/host-manage-hotel";
    }


    @PostMapping("/add-room")
    public String handleAddRoom(
            @RequestParam("hotelId") int hotelId,
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
            // Check for duplicate room title
            if (roomService.roomTitleExistsInHotel(roomTitle, hotelId)) {
                model.addAttribute("error", "Tên phòng đã tồn tại trong khách sạn này");
                return "redirect:/add-room?hotelId=" + hotelId + "&error=duplicate_name";
            }

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

            // Build and save room - set default room type ID to 1 for backward compatibility
            Room room = Room.builder()
                    .hotelId(hotelId)
                    .title(roomTitle)
                    .description(roomDescription)
                    .maxGuests(roomMaxGuests)
                    .quantity(roomQuantity)
                    .price(roomPrice)
                    .status("active")
                    .build();

            roomService.saveRoom(room, amenityIds, roomImageUrls);

            // Send notification to host
            User host = (User) session.getAttribute("user");
            if(host != null) {
                notificationService.notifyRoomAdded(host.getId(), roomTitle, hotelId);
            }

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

            // Check for duplicate room title (excluding current room)
            if (roomService.roomTitleExistsInHotelExcludingRoom(roomTitle, hotelId, roomId)) {
                response.put("success", false);
                response.put("message", "Tên phòng đã tồn tại trong khách sạn này");
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

            // Update room - keep existing room type or default to 1
            Room updatedRoom = Room.builder()
                    .roomId(roomId)
                    .hotelId(hotelId)
                    .title(roomTitle)
                    .description(description)
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

    @PostMapping("/deactivate-room")
    @ResponseBody
    public Map<String, Object> deactivateRoom(
            @RequestParam("roomId") int roomId,
            @RequestParam("hotelId") int hotelId,
            @RequestParam(value = "forceDeactivate", defaultValue = "false") boolean forceDeactivate,
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
                    response.put("message", "Không có quyền vô hiệu hóa phòng này");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error fetching hotel data for hotelId " + hotelId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra thông tin khách sạn");
                return response;
            }

            // 3. Check for approved bookings if not force deactivate
            if (!forceDeactivate) {
                int totalApprovedUnitsToReject = bookingService.countAllApprovedBookingUnitsInAffectedBookings(roomId);
                if (totalApprovedUnitsToReject > 0) {
                    response.put("success", false);
                    response.put("hasActiveBookings", true);
                    response.put("activeBookingsCount", totalApprovedUnitsToReject);
                    response.put("roomId", roomId);
                    response.put("message", "Vô hiệu hóa phòng này sẽ ảnh hưởng đến " + totalApprovedUnitsToReject + " phòng đã được duyệt " +
                            "(bao gồm tất cả phòng trong các booking có chứa phòng này). " +
                            "Nếu tiếp tục vô hiệu hóa, tất cả các phòng đã duyệt trong các booking bị ảnh hưởng sẽ bị hủy " +
                            "và bạn sẽ phải hoàn tiền cho khách hàng. Khách đã check-in sẽ không bị ảnh hưởng.");
                    return response;
                }
            }

            // 4. Check if this would leave no active rooms in the hotel
            List<Room> currentRooms;
            try {
                currentRooms = roomService.getRoomsByHotelId(hotelId);
                long activeRooms = currentRooms.stream()
                        .filter(room -> "active".equals(room.getStatus()))
                        .count();

                if (activeRooms <= 1) {
                    response.put("success", false);
                    response.put("message", "Không thể vô hiệu hóa phòng cuối cùng đang hoạt động. Khách sạn phải có ít nhất 1 phòng hoạt động.");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error fetching rooms for hotelId " + hotelId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra danh sách phòng");
                return response;
            }

            // 5. Always reject approved bookings when deactivating
            int rejectedBookings = 0;
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                List<Booking> bookings = bookingService.findActiveBookingsByRoomId(roomId);
                
                for(Booking booking : bookings){
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("id", String.valueOf(booking.getBookingId()));
                    params.add("trantype", "02");
                    params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
                    params.add("refundRole", "Hotel Owner");
                    params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

                    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
                    String res = restTemplate.postForObject(baseUrl + "/refund", request, String.class);

                    if(res != null && res.equals("00")){
                        rejectedBookings = bookingService.rejectAllActiveBookingsByRoomId(roomId);
                        notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), booking.refundAmount());
                    }else{
                        response.put("success", false);
                        response.put("message", "Hoàn tiền thất bại");
                        return response;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Lỗi khi hủy đặt phòng: " + e.getMessage());
                return response;
            }

            // 6. Deactivate the room
            try {
                roomService.deactivateRoom(roomId);
            } catch (Exception e) {
                System.err.println("Error deactivating room " + roomId + ": " + e.getMessage());
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Lỗi khi vô hiệu hóa phòng: " + e.getMessage());
                return response;
            }

            // 7. Success response
            String message = "Vô hiệu hóa phòng thành công";
            if (rejectedBookings > 0) {
                message += ". Đã hủy " + rejectedBookings + " phòng thành công.";
            }
            response.put("success", true);
            response.put("message", message);
            
        } catch (Exception e) {
            // Catch any unexpected errors
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi không mong muốn khi vô hiệu hóa phòng");
        }
        
        return response;
    }

    @PostMapping("/activate-room")
    @ResponseBody
    public Map<String, Object> activateRoom(
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
                    response.put("message", "Không có quyền kích hoạt phòng này");
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error fetching hotel data for hotelId " + hotelId + ": " + e.getMessage());
                response.put("success", false);
                response.put("message", "Lỗi khi kiểm tra thông tin khách sạn");
                return response;
            }

            // 3. Activate the room
            try {
                roomService.activateRoom(roomId);
                System.out.println("Successfully activated room " + roomId);
            } catch (Exception e) {
                System.err.println("Error activating room " + roomId + ": " + e.getMessage());
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Lỗi khi kích hoạt phòng: " + e.getMessage());
                return response;
            }

            // 4. Success response
            response.put("success", true);
            response.put("message", "Kích hoạt phòng thành công");
            
        } catch (Exception e) {
            // Catch any unexpected errors
            System.err.println("Unexpected error in activateRoom for roomId " + roomId + ": " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi không mong muốn khi kích hoạt phòng");
        }
        
        return response;
    }

    @PostMapping("/update-cancellation-policy")
    @ResponseBody
    public Map<String, Object> updateCancellationPolicy(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("partialRefundDays") int partialRefundDays,
            @RequestParam("partialRefundPercent") int partialRefundPercent,
            @RequestParam("noRefundWithinDays") int noRefundWithinDays,
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
                response.put("message", "Không có quyền chỉnh sửa chính sách của khách sạn này");
                return response;
            }

            // Create and validate cancellation policy
            CancellationPolicy policy = CancellationPolicy.builder()
                    .hotelId(hotelId)
                    .partialRefundDays(partialRefundDays)
                    .partialRefundPercent(partialRefundPercent)
                    .noRefundWithinDays(noRefundWithinDays)
                    .build();

            if (!cancellationPolicyService.validatePolicy(policy)) {
                response.put("success", false);
                response.put("message", "Chính sách hủy phòng không hợp lệ. Vui lòng kiểm tra lại các giá trị.");
                return response;
            }

            // Save or update the policy
            cancellationPolicyService.saveCancellationPolicy(policy);

            response.put("success", true);
            response.put("message", "Cập nhật chính sách hủy phòng thành công");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật chính sách: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/check-room-name")
    @ResponseBody
    public Map<String, Object> checkRoomName(
            @RequestParam("roomTitle") String roomTitle,
            @RequestParam("hotelId") int hotelId,
            @RequestParam(value = "roomId", required = false) Integer roomId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check authentication
            User host = (User) session.getAttribute("user");
            if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
                response.put("exists", false);
                response.put("message", "Unauthorized");
                return response;
            }

            // Check hotel ownership
            Hotel hotel = hotelService.getHotelById(hotelId);
            if (hotel == null || hotel.getHostId() != host.getId()) {
                response.put("exists", false);
                response.put("message", "Unauthorized hotel access");
                return response;
            }

            boolean exists;
            if (roomId != null) {
                // For editing existing room - exclude current room from check
                exists = roomService.roomTitleExistsInHotelExcludingRoom(roomTitle, hotelId, roomId);
            } else {
                // For adding new room
                exists = roomService.roomTitleExistsInHotel(roomTitle, hotelId);
            }

            response.put("exists", exists);
            if (exists) {
                response.put("message", "Tên phòng đã tồn tại trong khách sạn này");
            }

        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", "Error checking room name: " + e.getMessage());
        }

        return response;
    }

}
