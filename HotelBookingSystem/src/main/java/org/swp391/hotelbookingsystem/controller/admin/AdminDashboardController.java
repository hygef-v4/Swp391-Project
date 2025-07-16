package org.swp391.hotelbookingsystem.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminDashboardController {

    final
    BookingService bookingService;

    final
    HotelService hotelService;

    final
    RoomService roomService;

    final
    ReviewService reviewService;

    final
    UserService userService;

    final
    LocationService locationService;

    final CloudinaryService cloudinaryService;

    public AdminDashboardController(BookingService bookingService, HotelService hotelService, RoomService roomService, ReviewService reviewService, UserService userService, LocationService locationService, CloudinaryService cloudinaryService) {
        this.bookingService = bookingService;
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.locationService = locationService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/admin-dashboard")
    public String getDashboard(Model model, HttpSession session,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("pageTitle", "Hamora Booking");

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        int numberOfHotels = hotelService.countHotels();
        model.addAttribute("numberOfHotels", numberOfHotels);

        int totalRooms = roomService.countRooms();
        model.addAttribute("totalRooms", totalRooms);

        List<User> customerList = userService.getUsersByRole("CUSTOMER");
        model.addAttribute("numberOfCustomers", customerList.size());

        List<User> hotelOwnerList = userService.getUsersByRole("HOTEL_OWNER");
        model.addAttribute("numberOfHotelOwners", hotelOwnerList.size());

        List<Hotel> popularHotels = hotelService.getTop4PopularHotels();
        model.addAttribute("popularHotels", popularHotels);

        List<Location> allLocationStats = locationService.getAllLocationStats();

        int totalItems = allLocationStats.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Ensure valid page bounds
        page = Math.max(1, Math.min(page, totalPages));

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<Location> paginatedStats = allLocationStats.subList(fromIndex, toIndex);

        model.addAttribute("locationStats", paginatedStats);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);


        return "admin/admin-dashboard";
    }

    @PostMapping("/admin/locations/add")
    @ResponseBody
    public ResponseEntity<?> addLocation(@RequestParam("cityName") String cityName,
                                         @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (locationService.cityNameExists(cityName)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Tên thành phố đã tồn tại."));
            }

            Map<String, Object> uploadResult = cloudinaryService.uploadImage(imageFile, "locations");
            String imageUrl = uploadResult.get("secure_url").toString();

            Location location = new Location();
            location.setCityName(cityName.trim());
            location.setImageUrl(imageUrl);

            locationService.insertLocation(location);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Lỗi máy chủ: " + e.getMessage()));
        }
    }




}
