package org.swp391.hotelbookingsystem.controller.hotel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;


@Controller
public class HotelListController {
    @Autowired
    HotelService hotelService;
    @Autowired
    LocationService locationService;
    @Autowired
    AmenityService amenityService;

    @GetMapping("/hotel-list")
    public String hotelList(
        @RequestParam(value = "locationId", defaultValue = "-1") int locationId,
        @RequestParam(value = "dateRange", defaultValue = "") String dateRange,
        @RequestParam(value = "guests", defaultValue = "1") int guests,
        @RequestParam(value = "rooms", defaultValue = "1") int rooms,

        @RequestParam(value = "name", defaultValue = "") String name,
        @RequestParam(value = "min", defaultValue = "200,000") String min,
        @RequestParam(value = "max", defaultValue = "1,500,000") String max,

        @RequestParam(value = "star", defaultValue = "false") boolean star,

        @RequestParam(value = "page", defaultValue = "1") int page,
        Model model
    ){
        List<Location> location = locationService.getLocationById(locationId);
        if (locationId == -1 || location.isEmpty()) {
            model.addAttribute("hamora", new Location(locationId, "Hamora", "/assets/images/bg/05.jpg"));
        } else {
            model.addAttribute("hamora", location.get(0));
        }
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);

        model.addAttribute("dateRange", dateRange);
        String[] date = dateRange.split(" => ");
        Date checkin = !date[0].isBlank() ? Date.valueOf(date[0]) : null;
        Date checkout = date.length >= 2 ? Date.valueOf(date[1]) : checkin;

        model.addAttribute("guests", guests);
        model.addAttribute("rooms", rooms);

        int minPrice = Integer.parseInt(min.replace(",", ""));
        int maxPrice = Integer.parseInt(max.replace(",", ""));

        model.addAttribute("name", name.trim());
        model.addAttribute("min", minPrice/1000);
        model.addAttribute("max", maxPrice/1000);

        List<Hotel> hotel = hotelService.getHotelsByLocation(locationId, checkin, checkout, guests, rooms, name.trim(), minPrice, maxPrice, star);
        int item = page * 12;
        
        List<Hotel> current;
        if(hotel.size() < item){
            current = hotel.subList((page-1) * 12, hotel.size());
        }else current = hotel.subList((page-1) * 12, item);
        model.addAttribute("hotels", current);

        model.addAttribute("page", page);
        model.addAttribute("pagination", (int)Math.ceil((double)hotel.size() / 12));

        String redirect = "";
        if(locationId != -1) redirect += "&locationId=" + locationId;
        if(!"".equals(dateRange)) redirect += "&dateRange=" + URLEncoder.encode(dateRange, StandardCharsets.UTF_8);
        if(guests != 1) redirect += "&guests=" + guests;
        if(rooms != 1) redirect += "&rooms=" + rooms;
        model.addAttribute("redirect", redirect);

        String request = "";
        request += redirect;
        if(!"".equals(name)) request += "&name=" + name;
        if(!"200,000".equals(min)) request += "&min=" + min;
        if(!"15,000,000".equals(max)) request += "&max=" + max;
        model.addAttribute("request", request);

        model.addAttribute("star", star);

        return "page/hotelList";
    }
}
