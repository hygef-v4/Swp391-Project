package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/moderator-hotel-list")
public class ModeratorHotelListController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private UserService userService;

    @GetMapping("")
    public String getHotelList(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        Map<Integer, User> hostMap = new HashMap<>();
        int pendingCount = 0, approvedCount = 0, rejectedCount = 0;
        for (Hotel hotel : hotels) {
            // Đếm số lượng theo status
            if ("pending".equals(hotel.getStatus())) pendingCount++;
            else if ("active".equals(hotel.getStatus())) approvedCount++;
            else if ("inactive".equals(hotel.getStatus())) rejectedCount++;
            // Lấy thông tin chủ khách sạn
            if (!hostMap.containsKey(hotel.getHostId())) {
                User host = userService.findUserById(hotel.getHostId());
                if (host != null) hostMap.put(hotel.getHostId(), host);
            }
        }
        model.addAttribute("hotels", hotels);
        model.addAttribute("hostMap", hostMap);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        return "moderator/moderatorHotelList";
    }

    // REST API cho các thao tác duyệt, từ chối, xem chi tiết
    @PostMapping("/api/moderator/hotels/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveHotel(@PathVariable int id) {
        hotelService.updateHotelStatus(id, "active");
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Phê duyệt khách sạn thành công!");
        return res;
    }

    @PostMapping("/api/moderator/hotels/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectHotel(@PathVariable int id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        hotelService.updateHotelStatus(id, "inactive");
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Từ chối khách sạn thành công!");
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
