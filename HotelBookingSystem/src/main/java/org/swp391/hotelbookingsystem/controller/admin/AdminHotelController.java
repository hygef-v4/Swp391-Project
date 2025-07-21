package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class AdminHotelController {

    @Value("${app.base-url}")
    private String baseUrl;

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

    final
    AmenityService amenityService;

    final
    BookingService bookingService;

    final
    NotificationService notificationService;

    public AdminHotelController(HotelService hotelService, RoomService roomService, ReviewService reviewService, UserService userService, LocationService locationService, AmenityService amenityService, BookingService bookingService, NotificationService notificationService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin-hotel-list")
    public String getHotelDashboard(@RequestParam(value = "search", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    Model model, HttpSession session) {
        model.addAttribute("pageTitle", "Hamora Booking - Hotel Management");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String trimmedSearch = (search != null) ? search.trim().replaceAll("\\s+", " ") : null;

        List<Hotel> filteredHotels;
        if (trimmedSearch != null && !trimmedSearch.isEmpty()) {
            filteredHotels = hotelService.searchHotel(search);
        } else {
            filteredHotels = hotelService.getAllHotels();
        }

        int pageSize = 12;
        int totalHotels = filteredHotels.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalHotels);
        List<Hotel> currentHotels = (startIndex < totalHotels) ? filteredHotels.subList(startIndex, endIndex) : List.of();

        model.addAttribute("search", trimmedSearch);
        model.addAttribute("hotelList", currentHotels);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalHotels / pageSize));
        model.addAttribute("startIndex", startIndex); // +1 to match display
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalHotels", totalHotels);


        return "admin/admin-hotel-list";
    }

    @GetMapping("/admin/hotel/view/{hotelId}")
    public String hotelDetail(
            @PathVariable("hotelId") int hotelId,
            @RequestParam(value = "rooms", defaultValue = "1") int roomQuantity,

            Model model, HttpSession session
    ){
        model.addAttribute("roomQuantity", roomQuantity);

        Hotel hotel = hotelService.getHotelById(hotelId);
        // Set hotel owner name
        User owner = userService.findUserById(hotel.getHostId());
        if (owner != null) {
            hotel.setHostName(owner.getFullName());
        }
        hotel.setPolicy(hotel.getPolicy().replace("<li>", "<li class=\"list-group-item d-flex\"><i class=\"bi bi-arrow-right me-2\"></i>"));
        model.addAttribute("hotel", hotel);

        User user = (User) session.getAttribute("user");
        boolean favorite = false;
        if (user != null) {
            favorite = hotelService.isFavoriteHotel(user.getId(), hotelId);
        }model.addAttribute("favorite", favorite);

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

            int count = bookingService.countBookingsByRoomId(room.getRoomId());
            room.setBookedCount(count);
            room.setCategories(categories);
        }
        model.addAttribute("rooms", rooms);

        int roomCount = rooms.stream().mapToInt(Room::getQuantity).sum();
        model.addAttribute("roomCount", roomCount);

        int roomTypeCount = rooms.size();
        model.addAttribute("roomTypeCount", roomTypeCount);

        int totalBookings = hotelService.countBookingsByHotelId(hotelId);
        model.addAttribute("totalBookings", totalBookings);

        String hotelName = hotel.getHotelName();
        String encode = URLEncoder.encode(hotelName, StandardCharsets.UTF_8);
        String map = "https://www.google.com/maps/search/" + encode + "//@" + hotel.getLatitude() + "," + hotel.getLongitude() + ",17z";
        model.addAttribute("map", map);
        return "admin/admin-hotel-detail";
    }

    @PostMapping("/admin/hotel/ban/{hotelId}")
    public String banHotel(@PathVariable("hotelId") int hotelId,
                           @RequestParam("reason") String reason,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        List<Booking> bookings = bookingService.findActiveBookingsByHotelId(hotelId);

        for(Booking booking : bookings){
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", String.valueOf(booking.getBookingId()));
            params.add("trantype", "02");
            params.add("amount", String.valueOf(booking.getTotalPrice().longValue()));
            params.add("refundRole", "Hotel Owner");
            params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            String response = restTemplate.postForObject(baseUrl + "/refund", request, String.class);

            if(response != null && response.equals("00")){
                notificationService.rejectNotification(booking.getCustomerId(), String.valueOf(booking.getBookingId()), booking.refundAmount());
            }else{
                redirectAttributes.addFlashAttribute("errorMessage", "Hoàn tiền thất bại");
                return "redirect:/admin/hotel/view/" + hotelId;
            }
        }

        boolean result = hotelService.banHotel(hotelId, reason, admin.getId());

        if (result) {
            redirectAttributes.addFlashAttribute("successMessage", "Khách sạn đã bị cấm.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cấm khách sạn.");
        }

        return "redirect:/admin/hotel/view/" + hotelId;
    }

    @PostMapping("/admin/hotel/unban/{hotelId}")
    public String unbanHotel(@PathVariable("hotelId") int hotelId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        int adminId = ((User) session.getAttribute("user")).getId();
        try {
            boolean success = hotelService.unbanHotel(hotelId, adminId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ cấm khách sạn và gửi thông báo.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể gỡ cấm khách sạn.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/hotel/view/" + hotelId;
    }
}