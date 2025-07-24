package org.swp391.hotelbookingsystem.controller.booking;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.AmenityCategory;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.Coupon;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.AmenityService;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.CouponService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.LocationService;
import org.swp391.hotelbookingsystem.service.RoomService;
import org.swp391.hotelbookingsystem.service.VNPayService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {
    @Autowired
    BookingService bookingService;
    @Autowired
    LocationService locationService;
    @Autowired
    HotelService hotelService;
    @Autowired
    RoomService roomService;
    @Autowired
    AmenityService amenityService;
    @Autowired
    CouponService couponService;

    @GetMapping("/booking/{id}")
    public String booking(
        @PathVariable(value = "id") int hotelId,

        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int roomQuantity,    
        
        @RequestParam(value = "bookingId", required = false) Integer bookingId, 
        Model model, HttpSession session
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }model.addAttribute("user", user);

        if(bookingId != null){
            try{
                Booking booking = bookingService.findById(bookingId);
                if(booking == null) return "redirect:/";
                else bookingService.deletePendingBooking(bookingId, user.getId());
            }catch(Exception e){}
        }

        Hotel hotel = hotelService.getHotelById(hotelId);
        hotel.setPolicy(hotel.getPolicy().replace("<li>", "<li class=\"list-group-item d-flex\"><i class=\"bi bi-arrow-right me-2\"></i>"));
        model.addAttribute("hotel", hotel);

        model.addAttribute("dateRange", dateRange);
        String[] date = dateRange.split(" => ");
        Date checkin = !date[0].isBlank() ? Date.valueOf(date[0]) : null;
        Date checkout = date.length > 1 ? Date.valueOf(date[1]) : checkin;

        model.addAttribute("checkIn", date[0]);
        model.addAttribute("checkOut", date.length > 1 ? date[1] : date[0]);

        long night = ChronoUnit.DAYS.between(checkin.toLocalDate(), checkout.toLocalDate());
        model.addAttribute("night", night);

        model.addAttribute("guests", guests);
        model.addAttribute("roomQuantity", roomQuantity);
        
        List<Room> rooms = roomService.getRoomsByIdAndDateRange(hotelId, checkin, checkout);
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

            room.setCategories(categories);
        }model.addAttribute("rooms", rooms);

        String hotelName = hotel.getHotelName();
        String encode = URLEncoder.encode(hotelName, StandardCharsets.UTF_8);
        String map = "https://www.google.com/maps/search/" + encode + "//@" + hotel.getLatitude() + "," + hotel.getLongitude() + ",17z";
        model.addAttribute("map", map);

        return "page/booking";
    }

    @PostMapping("/booking")
    public String bookingPayment(
        @RequestParam(value = "hotelId") int hotelId,
        @RequestParam(value = "checkIn") String checkIn,
        @RequestParam(value = "checkOut") String checkOut,
        @RequestParam(value = "totalPrice") double totalPrice,
        @RequestParam(value = "couponCode", required = false) String couponCode,

        @RequestParam(value = "roomId") List<Integer> roomId,
        @RequestParam(value = "roomName") List<String> roomName,
        @RequestParam(value = "price") List<Double> price,
        @RequestParam(value = "quantity") List<Integer> quantity,

        @RequestParam(value = "dateRange") String dateRange,
        @RequestParam(value = "guests") int guests,
        @RequestParam(value = "rooms") int rooms,

        Model model, HttpSession session, HttpServletResponse response
    ){
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        Hotel hotel = hotelService.getHotelById(hotelId);

        Coupon coupon = couponService.searchCouponsByCode(couponCode);
        Integer couponId = coupon == null ? null : coupon.getCouponId();

        Booking booking = Booking.builder()
            .customerId(user.getId())
            .customerName(user.getFullName())
            .hotelId(hotelId)
            .hotelName(hotel.getHotelName())
            .imageUrl(hotel.getHotelImageUrl())
            .checkIn(LocalDate.parse(checkIn).atTime(14, 0))
            .checkOut(LocalDate.parse(checkOut).atTime(12, 0))
            .totalPrice(totalPrice)
            .createdAt(LocalDateTime.now())
            .couponId(couponId)
            .build();
            
        List<String> json = new ArrayList<>();
        List<BookingUnit> bookingUnits = new ArrayList<>();

        for(int i = 0; i < roomId.size(); i++){
            BookingUnit bookingUnit = BookingUnit.builder()
                .roomId(roomId.get(i))
                .roomName(roomName.get(i))
                .price(price.get(i))
                .quantity(quantity.get(i))
                .build();
            bookingUnits.add(bookingUnit);

            json.add(String.format("{\"roomId\":\"%d\",\"quantity\":%d}", roomId.get(i), quantity.get(i)));
        }booking.setBookingUnits(bookingUnits);

        Cookie cookie = new Cookie("booking" + hotelId, URLEncoder.encode("[" + String.join(",", json) + "]", StandardCharsets.UTF_8));
        cookie.setPath("/");
        cookie.setMaxAge(60 * 30);
        response.addCookie(cookie);

        if(!bookingService.checkQuantity(booking) || couponService.checkCoupon(couponId)){
            return "redirect:/booking/" + hotelId + "?dateRange=" + dateRange + "&guests=" + guests + "&rooms=" + rooms;
        }

        if(couponId != null){
            couponService.applyCoupon(couponId, user.getId());
        }
        
        int id = bookingService.pendingBooking(booking);
        return "redirect:/payment/" + id + "?hotelId=" + hotelId + "&dateRange=" + dateRange + "&guests=" + guests + "&rooms=" + rooms;
    }
}
