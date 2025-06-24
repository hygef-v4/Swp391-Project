package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminAgentController {

    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;

    public AdminAgentController(UserService userService, HotelService hotelService, BookingService bookingService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }


    @GetMapping("/admin-agent-list")
    public String showAgentList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "8") int size,
                                @RequestParam(required = false, defaultValue = "") String search,
                                Model model) {

        int offset = (page - 1) * size;
        List<User> agentList = userService.getAgentsBySearchPaginated(search, offset, size);
        int totalItems = userService.countAgentsBySearch(search);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        Map<Integer, Integer> hotelCountMap = new HashMap<>();
        for (User agent : agentList) {
            int count = hotelService.countHotelsByHostId(agent.getId());
            hotelCountMap.put(agent.getId(), count);
        }

        int totalAgent = userService.countAgent();

        model.addAttribute("totalAgent", totalAgent);
        model.addAttribute("agentList", agentList);
        model.addAttribute("hotelCountMap", hotelCountMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("search", search);

        return "admin/admin-agent-list";
    }



    @GetMapping("/admin-agent-detail")
    public String showAgentDetail(@RequestParam("id") int agentId, Model model) {
        User agent = userService.findUserById(agentId);

        List<Hotel> hotelList = hotelService.getHotelsByHostId(agent.getId());
        int countHotel = hotelList.size();
        int totalBooking = bookingService.countBookingsByHostId(agent.getId());


        model.addAttribute("totalBooking", totalBooking);
        model.addAttribute("countHotel", countHotel);
        model.addAttribute("hotelList", hotelList);
        model.addAttribute("agent", agent);
        return "admin/admin-agent-detail";
    }

}
