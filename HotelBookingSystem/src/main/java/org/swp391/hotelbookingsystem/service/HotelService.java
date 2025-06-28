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

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel getHotelById(int id){
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
}
