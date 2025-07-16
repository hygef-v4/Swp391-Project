package org.swp391.hotelbookingsystem.controller.moderator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.UserService;

@Controller
@RequestMapping("/moderator-hotel-list")
public class ModeratorHotelListController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("")
    public String getHotelList(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        
        // Sắp xếp khách sạn: pending -> active -> inactive
        List<Hotel> pendingHotels = hotels.stream()
            .filter(h -> "pending".equals(h.getStatus()))
            .toList();
        List<Hotel> activeHotels = hotels.stream()
            .filter(h -> "active".equals(h.getStatus()))
            .toList();
        List<Hotel> inactiveHotels = hotels.stream()
            .filter(h -> "inactive".equals(h.getStatus()))
            .toList();
        List<Hotel> bannedHotels = hotels.stream()
            .filter(h -> "banned".equals(h.getStatus()))
            .toList();

        // Ghép các danh sách theo thứ tự ưu tiên
        List<Hotel> sortedHotels = new ArrayList<>();
        sortedHotels.addAll(pendingHotels);
        sortedHotels.addAll(activeHotels);
        sortedHotels.addAll(inactiveHotels);
        sortedHotels.addAll(bannedHotels);

        // Lấy danh sách thành phố duy nhất và sắp xếp theo alphabet
        List<String> cities = hotels.stream()
            .map(Hotel::getCityName)
            .filter(city -> city != null && !city.trim().isEmpty())
            .distinct()
            .sorted()
            .toList();

        Map<Integer, User> hostMap = new HashMap<>();
        int pendingCount = pendingHotels.size();
        int approvedCount = activeHotels.size();
        int rejectedCount = inactiveHotels.size(); // dừng hoạt động
        int bannedCount = bannedHotels.size(); // bị khóa

        // Get host info
        for (Hotel hotel : sortedHotels) {
            if (!hostMap.containsKey(hotel.getHostId())) {
                User host = userService.findUserById(hotel.getHostId());
                if (host != null) hostMap.put(hotel.getHostId(), host);
            }
        }

        model.addAttribute("hotels", sortedHotels);
        model.addAttribute("cities", cities);
        model.addAttribute("hostMap", hostMap);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("bannedCount", bannedCount);
        return "moderator/moderatorHotelList";
    }

    // REST API cho các thao tác duyệt, từ chối, xem chi tiết
    @PostMapping("/api/moderator/hotels/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveHotel(@PathVariable int id) {
        hotelService.updateHotelStatus(id, "active");
        // Notify hotel owner
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            notificationService.notifyHotelApproval(hotel.getHostId(), hotel.getHotelName());
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Phê duyệt khách sạn thành công!");
        return res;
    }

    @PostMapping("/api/moderator/hotels/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectHotel(@PathVariable int id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        hotelService.updateHotelStatus(id, "banned");
        // Notify hotel owner
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            String message = "Khách sạn của bạn đã bị từ chối. Lý do: " + (reason != null ? reason : "Không xác định");
            notificationService.createNotification(
                hotel.getHostId(),
                "Khách sạn bị từ chối",
                message,
                "hotel",
                "high",
                "/host-listing",
                "bi-x-octagon",
                Map.of("hotelId", hotel.getHotelId(), "hotelName", hotel.getHotelName(), "reason", reason)
            );
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Từ chối khách sạn thành công!");
        return res;
    }

    @PostMapping("/api/moderator/hotels/{id}/unlock")
    @ResponseBody
    public Map<String, Object> unlockHotel(@PathVariable int id) {
        hotelService.updateHotelStatus(id, "active");
        // Notify hotel owner
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            notificationService.createNotification(
                hotel.getHostId(),
                "Khách sạn được mở khóa",
                "Khách sạn của bạn đã được mở khóa và hoạt động trở lại.",
                "hotel",
                "info",
                "/host-listing",
                "bi-unlock",
                Map.of("hotelId", hotel.getHotelId(), "hotelName", hotel.getHotelName())
            );
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Mở khóa khách sạn thành công!");
        return res;
    }

    @GetMapping("/api/moderator/hotels/{id}/details")
    @ResponseBody
    public Map<String, Object> getHotelDetails(@PathVariable int id) {
        Hotel hotel = hotelService.getHotelById(id);
        Map<String, Object> res = new HashMap<>();
        if (hotel != null) {
            User host = userService.findUserById(hotel.getHostId());
            res.put("success", true);
            res.put("hotel", hotel);
            res.put("host", host);
        } else {
            res.put("success", false);
            res.put("message", "Không tìm thấy khách sạn");
        }
        return res;
    }
}