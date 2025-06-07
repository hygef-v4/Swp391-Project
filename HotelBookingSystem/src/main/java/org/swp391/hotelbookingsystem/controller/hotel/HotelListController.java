package org.swp391.hotelbookingsystem.controller.hotel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class HotelListController {
    @Autowired
    HotelService hotelService;
    @Autowired
    LocationService locationService;

    @GetMapping("/hotel-list")
    public String hotelList(
        @RequestParam(value = "locationId", defaultValue = "-1") int locationId,
        @RequestParam(value = "adults", defaultValue = "1") int adults,
        @RequestParam(value = "children", defaultValue = "0") int children,
        @RequestParam(value = "rooms", defaultValue = "1") int rooms,

        @RequestParam(value = "name", defaultValue = "") String name,
        @RequestParam(value = "min", defaultValue = "200,000") String min,
        @RequestParam(value = "max", defaultValue = "1,500,000") String max,

        @RequestParam(value = "page", defaultValue = "1") int page,
        Model model
    ){
        List<Location> location = locationService.getLocationById(locationId);
        model.addAttribute("hamora", locationId == -1 ? new Location(locationId, "Hamora", "assets/images/bg/05.jpg") : location.get(0));
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
        model.addAttribute("rooms", rooms);

        int minPrice = Integer.parseInt(min.replace(",", ""));
        int maxPrice = Integer.parseInt(max.replace(",", ""));

        model.addAttribute("name", name.trim());
        model.addAttribute("min", minPrice/1000);
        model.addAttribute("max", maxPrice/1000);

        List<Hotel> hotel = hotelService.getHotelsByLocation(locationId, (adults + children), rooms, name.trim(), minPrice, maxPrice);
        int item = page * 12;
        
        List<Hotel> current;
        if(hotel.size() < item){
            current = hotel.subList((page-1) * 12, hotel.size());
        }else current = hotel.subList((page-1) * 12, item);
        model.addAttribute("hotels", current);

        model.addAttribute("page", page);
        model.addAttribute("pagination", (int)Math.ceil((double)hotel.size() / 12));

        String redirect = "";
        if(adults != 1) redirect += "&adults=" + adults;
        if(children != 0) redirect += "&children=" + children;
        if(rooms != 1) redirect += "&rooms=" + rooms;
        model.addAttribute("redirect", redirect);

        String request = "";
        if(locationId != -1) request += "&locationId=" + locationId;
        if(adults != 1) request += "&adults=" + adults;
        if(children != 0) request += "&children=" + children;
        if(rooms != 1) request += "&rooms=" + rooms;
        if(!"".equals(name)) request += "&name=" + name;
        if(!"200,000".equals(min)) request += "&min=" + min;
        if(!"15,000,000".equals(max)) request += "&max=" + max;
        model.addAttribute("request", request);

        return "page/hotelList";
    }

    @ResponseBody
    @PostMapping("/filter-hotels")
    public List<Object> filterHotels(
            @RequestParam(value = "locationId", defaultValue = "-1") int locationId,
            @RequestParam(value = "adults", defaultValue = "1") int adults,
            @RequestParam(value = "children", defaultValue = "0") int children,
            @RequestParam(value = "rooms", defaultValue = "1") int rooms,

            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "min", defaultValue = "200,000") String min,
            @RequestParam(value = "max", defaultValue = "15,000,000") String max
    ){
        int minPrice = Integer.parseInt(min.replace(",", ""));
        int maxPrice = Integer.parseInt(max.replace(",", ""));
        List<Hotel> hotel = hotelService.getHotelsByLocation(locationId, (adults + children), rooms, name.trim(), minPrice, maxPrice);
        
        List<Hotel> current;
        if(hotel.size() < 12){
            current = hotel.subList(0, hotel.size());
        }else current = hotel.subList(0, 12);

        List<Object> json = new ArrayList<>();
        json.add(1);
        json.add((int)Math.ceil((double)hotel.size() / 12));
        json.add(current);

        return json;
    }
    
}
