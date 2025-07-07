package org.swp391.hotelbookingsystem.service;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.repository.HotelRepository;

import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final NotificationService notificationService;

    public HotelService(HotelRepository hotelRepository, NotificationService notificationService) {
        this.hotelRepository = hotelRepository;
        this.notificationService = notificationService;
    }

    public Hotel getHotelById(int id) {
        return hotelRepository.getHotelById(id);
    }

    public List<Hotel> getHotelsSortedByRating() {
        return hotelRepository.getHotelsSortedByRating();
    }

    public List<Hotel> getTop8HighRatedHotels() {
        return hotelRepository.getTop8HighRatedHotels();
    }

    public List<Hotel> getHotelsByLocation(int id, Date checkin, Date checkout, int maxGuests, int roomQuantity, String name, int min, int max, boolean star) {
        return hotelRepository.getHotelsByLocation(id, checkin, checkout, maxGuests, roomQuantity, name, min, max, star);
    }

    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.insertHotel(hotel);
    }

    public void updateHotel(Hotel hotel) {
        hotelRepository.updateHotel(hotel);
    }

    public int countHotels() {
        return hotelRepository.countHotels();
    }

    public List<Hotel> getTop4PopularHotels() {
        return hotelRepository.getTop4PopularHotels();
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.getAllHotels();
    }

    public List<Hotel> getHotelsByHostId(int hostId) {
        return hotelRepository.findByHostId(hostId);
    }

    public List<Hotel> searchHotel(String keyword) {
        return hotelRepository.searchHotel(keyword);
    }

    public void deleteById(int hotelId) {
        hotelRepository.deleteHotelById(hotelId);
    }

    public boolean isFavoriteHotel(int userId, int hotelId) {
        return hotelRepository.isFavoriteHotel(userId, hotelId) != 0;
    }

    public void insertHotelDeletionToken(int userId, String token, LocalDateTime expiry, String tokenType) {
        hotelRepository.insertHotelDeletionToken(userId, token, expiry, tokenType);
    }

    public String getHotelDeleteTokenType(String token, int userId) {
        return hotelRepository.findValidTokenType(token, userId);
    }


    public void cancelHotelDeleteToken(int userId, int hotelId) {
        hotelRepository.cancelHotelDeleteToken(userId, hotelId);
    }

    public int countHotelsByHostId(int id) {
        return hotelRepository.countHotelByHostId(id);
    }

    public int getTotalHotels() {
        try {
            return hotelRepository.getAllHotels().size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Hotel> searchHotelsPaginated(String search, int offset, int size) {
        return hotelRepository.searchHotelsPaginated(search, offset, size);
    }

    public int countHotelsBySearch(String search) {
        return hotelRepository.countHotelsBySearch(search);
    }

    public boolean banHotel(int hotelId, String reason, int adminId) {
        Hotel hotel = hotelRepository.getHotelById(hotelId);
        if (hotel == null) return false;

        int updated = hotelRepository.updateHotelStatus(hotelId, "banned");
        if (updated <= 0) return false;

        String message = "Khách sạn \"" + hotel.getHotelName() + "\" đã bị cấm. Lý do: " + reason;
        notificationService.notifyUser(hotel.getHostId(), message);

        return true;
    }

    public void updateHotelStatus(int hotelId, String status) {
        hotelRepository.updateHotelStatus(hotelId, status);
    }

    public boolean unbanHotel(int hotelId, int adminId) {
        Hotel hotel = hotelRepository.getHotelById(hotelId);
        if (hotel == null) return false;

        int updated = hotelRepository.updateHotelStatus(hotelId, "active");
        if (updated <= 0) return false;

        String message = "Khách sạn \"" + hotel.getHotelName() + "\" đã được gỡ cấm và hiện có thể hoạt động trở lại.";
        notificationService.notifyUser(hotel.getHostId(), message);

        return true;
    }

    public Double getAverageRatingByHotelId(int hotelId) {
        Double avgRating = hotelRepository.findAverageRatingByHostId(hotelId);
        if (avgRating == null) {
            return 0.0;
        }
        return Math.round(avgRating * 10.0) / 10.0;
    }

    public int countBookingsByHotelId(int hotelId) {
        return hotelRepository.countBookingsByHotelId(hotelId);

    }
}
