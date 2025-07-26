package org.swp391.hotelbookingsystem.controller.host;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.CancellationPolicy;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.CancellationPolicyService;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HostRegisterController {
    final LocationService locationService;
    final AmenityService amenityService;
    final CloudinaryService cloudinaryService;
    final RoomService roomService;
    final HotelService hotelService;
    final UserService userService;
    final CancellationPolicyService cancellationPolicyService;
    final NotificationService notificationService;
    final ReviewService reviewService;

    public HostRegisterController(LocationService locationService, AmenityService amenityService,
            CloudinaryService cloudinaryService, RoomService roomService, HotelService hotelService,
            UserService userService, CancellationPolicyService cancellationPolicyService, NotificationService notificationService,
            ReviewService reviewService) {
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.cloudinaryService = cloudinaryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.cancellationPolicyService = cancellationPolicyService;
        this.notificationService = notificationService;
        this.reviewService = reviewService;
    }

    @GetMapping("/host-intro")
    public String showHostIntroPage(Model model) {
        // Fetch top 5 public positive reviews and add to model (same as homepage)
        List<Review> top5Reviews = reviewService.getTop5PublicPositiveReviews();
        model.addAttribute("top5Reviews", top5Reviews);

        return "page/host-intro";
    }

    @GetMapping("/register-host")
    public String showRegisterHostPage(Model model, HttpSession session,
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) String hotelDescription,
            @RequestParam(required = false) String hotelAddress,
            @RequestParam(required = false) Integer hotelLocation,
            @RequestParam(required = false) String hotelLocationText,
            @RequestParam(required = false) String hotelLatitude,
            @RequestParam(required = false) String hotelLongitude,
            @RequestParam(required = false) String hotelPolicies,
            @RequestParam(required = false) String roomTitle,
            @RequestParam(required = false) Integer roomMaxGuests,
            @RequestParam(required = false) Integer roomQuantity,
            @RequestParam(required = false) Float roomPrice,
            @RequestParam(required = false) String roomDescription,
            @RequestParam(required = false) List<Integer> selectedAmenities,
            @RequestParam(required = false) Integer partialRefundDays,
            @RequestParam(required = false) Integer partialRefundPercent,
            @RequestParam(required = false) Integer noRefundWithinDays) {

        // Check if user is logged in
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Skip validation if user just completed profile update
        String profileJustUpdated = (String) session.getAttribute("profileJustUpdated");
        if (!"true".equals(profileJustUpdated)) {
            // Validate profile completeness before allowing hotel registration
            UserService.ProfileValidationResult validationResult = userService.validateProfileCompleteness(currentUser);
            if (!validationResult.isComplete()) {
                // Debug logging
                System.out.println("=== HOTEL REGISTRATION VALIDATION ===");
                System.out.println("User ID: " + currentUser.getId());
                System.out.println("Missing Fields: " + validationResult.getMissingFields());
                System.out.println("Message: " + validationResult.getMessage());

                // Check if only payment information is missing
                List<String> missingFields = validationResult.getMissingFields();
                if (missingFields.size() == 1 && missingFields.get(0).contains("thanh toán")) {
                    // Only payment info missing - redirect directly to payment page
                    System.out.println("Redirecting to payment page - only payment missing");
                    session.setAttribute("returnToAfterProfileUpdate", "hotel_registration");
                    return "redirect:/payment-information?incomplete=true&reason=hotel_registration";
                } else {
                    // Multiple fields missing - redirect to profile page
                    System.out.println("Redirecting to profile page - multiple fields missing");
                    session.setAttribute("profileValidationMessage", validationResult.getMessage());
                    session.setAttribute("missingFields", validationResult.getMissingFields());
                    session.setAttribute("redirectReason", "hotel_registration");
                    session.setAttribute("returnToAfterProfileUpdate", "hotel_registration");
                    return "redirect:/user-profile?incomplete=true&reason=hotel_registration";
                }
            }
        } else {
            // Clear the flag after use
            session.removeAttribute("profileJustUpdated");
        }

        session.setAttribute("locations", locationService.getAllLocations());

        //  Get amenities with joined category
        List<Amenity> amenities = amenityService.getAllAmenitiesWithCategory();

        //  Group by category name instead of ID
        Map<String, List<Amenity>> groupedAmenities = new LinkedHashMap<>();  // use LinkedHashMap to maintain insertion order
        for (Amenity amenity : amenities) {
            String categoryName = amenity.getCategory().getName();

            groupedAmenities
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(amenity);
        }

        model.addAttribute("groupedAmenities", groupedAmenities);
        
        // Add draft data to model if present
        if (hotelName != null) model.addAttribute("hotelName", hotelName);
        if (hotelDescription != null) model.addAttribute("hotelDescription", hotelDescription);
        if (hotelAddress != null) model.addAttribute("hotelAddress", hotelAddress);
        if (hotelLocation != null) model.addAttribute("hotelLocation", hotelLocation);
        if (hotelLocationText != null) model.addAttribute("hotelLocationText", hotelLocationText);
        if (hotelLatitude != null) model.addAttribute("hotelLatitude", hotelLatitude);
        if (hotelLongitude != null) model.addAttribute("hotelLongitude", hotelLongitude);
        if (hotelPolicies != null) model.addAttribute("hotelPolicies", hotelPolicies);
        if (roomTitle != null) model.addAttribute("roomTitle", roomTitle);
        if (roomMaxGuests != null) model.addAttribute("roomMaxGuests", roomMaxGuests);
        if (roomQuantity != null) model.addAttribute("roomQuantity", roomQuantity);
        if (roomPrice != null) model.addAttribute("roomPrice", roomPrice);
        if (roomDescription != null) model.addAttribute("roomDescription", roomDescription);
        if (selectedAmenities != null) model.addAttribute("selectedAmenities", selectedAmenities);
        
        // Add cancellation policy draft data
        if (partialRefundDays != null) model.addAttribute("partialRefundDays", partialRefundDays);
        if (partialRefundPercent != null) model.addAttribute("partialRefundPercent", partialRefundPercent);
        if (noRefundWithinDays != null) model.addAttribute("noRefundWithinDays", noRefundWithinDays);
        
        return "page/register-host";
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

            @RequestParam("roomTitle") String roomTitle,
            @RequestParam("roomMaxGuests") int roomMaxGuests,
            @RequestParam("roomQuantity") int roomQuantity,
            @RequestParam("roomPrice") float roomPrice,
            @RequestParam("roomDescription") String roomDescription,
            @RequestParam(value = "amenities", required = false) List<Integer> amenityIds,

            @RequestParam("roomImageFiles") MultipartFile[] roomImageFiles,

            // Cancellation policy parameters
            @RequestParam("partialRefundDays") int partialRefundDays,
            @RequestParam("partialRefundPercent") int partialRefundPercent,
            @RequestParam("noRefundWithinDays") int noRefundWithinDays,

            HttpSession session,
            Model model
    ) {
        try {
            User user = (User) session.getAttribute("user");
            int userId = user.getId();

            boolean becameHost = false;
            if (!"HOTEL_OWNER".equalsIgnoreCase(user.getRole())) {
                user.setRole("HOTEL_OWNER");
                userService.updateUserRoleToHost(userId);
                session.setAttribute("user", user);
                becameHost = true;

                // Update Spring Security context
                List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
                updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_HOTEL_OWNER"));
                Authentication newAuth = new UsernamePasswordAuthenticationToken(user, null, updatedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            // Validate cancellation policy data
            CancellationPolicy cancellationPolicy = CancellationPolicy.builder()
                    .partialRefundDays(partialRefundDays)
                    .partialRefundPercent(partialRefundPercent)
                    .noRefundWithinDays(noRefundWithinDays)
                    .build();

            if (!cancellationPolicyService.validatePolicy(cancellationPolicy)) {
                model.addAttribute("error", "Chính sách hủy phòng không hợp lệ. Vui lòng kiểm tra lại các giá trị đã nhập.");
                return "page/register-host";
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
                    .status("pending") // Set status to pending
                    .build(); // no .rating() // because it's optional


            Hotel savedHotel = hotelService.saveHotel(hotel);

            // Save cancellation policy for the hotel
            cancellationPolicy.setHotelId(savedHotel.getHotelId());
            cancellationPolicyService.saveCancellationPolicy(cancellationPolicy);

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


            // room - set default room type ID to 1 for backward compatibility
            Room room = Room.builder()
                    .hotelId(savedHotel.getHotelId())
                    .title(roomTitle)
                    .description(roomDescription)
                    .maxGuests(roomMaxGuests)
                    .quantity(roomQuantity)
                    .price(roomPrice)
                    .status("active")
                    .build();

            roomService.saveRoom(room, amenityIds, roomImageUrls);

            // Send notifications
            if (becameHost) {
                notificationService.notifyHostRegistrationSuccess(userId);
            }
            notificationService.notifyHotelAdded(userId, savedHotel.getHotelName(), savedHotel.getHotelId());
            // Notify all moderators about the new pending hotel
            User host = userService.findUserById(userId);
            String hostName = host != null ? host.getFullName() : "Unknown";
            notificationService.notifyAllModeratorsNewHotelApproval(savedHotel.getHotelName(), hostName, savedHotel.getHotelId());

            return "redirect:/host-dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi khi tạo khách sạn: " + e.getMessage());
            return "page/register-host";
        }
    }
}
