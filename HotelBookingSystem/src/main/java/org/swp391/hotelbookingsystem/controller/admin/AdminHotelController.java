package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;
import org.swp391.hotelbookingsystem.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.service.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class AdminHotelController {

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

    public AdminHotelController(HotelService hotelService, RoomService roomService, ReviewService reviewService, UserService userService, LocationService locationService, AmenityService amenityService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.locationService = locationService;
        this.amenityService = amenityService;
        this.bookingService = bookingService;
    }

    @GetMapping("/admin-hotel-list")
    public String getHotelDashboard(@RequestParam(value = "search", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    Model model, HttpSession session) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora Booking - Hotel Management");
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
            @RequestParam(value = "dateRange", defaultValue = "") String dateRange,
            @RequestParam(value = "adults", defaultValue = "1") int adults,
            @RequestParam(value = "children", defaultValue = "0") int children,
            @RequestParam(value = "rooms", defaultValue = "1") int roomQuantity,

            Model model, HttpSession session
    ){
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("dateRange", dateRange);

        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
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
        if(!"".equals(dateRange)) redirect += "&dateRange=" + dateRange;
        if(adults != 1) redirect += "&adults=" + adults;
        if(children != 0) redirect += "&children=" + children;
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
        boolean result = hotelService.banHotel(hotelId, reason, admin.getId());

        if (result) {
            redirectAttributes.addFlashAttribute("successMessage", "Khách sạn đã bị cấm và thông báo đã được ghi nhận.");
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