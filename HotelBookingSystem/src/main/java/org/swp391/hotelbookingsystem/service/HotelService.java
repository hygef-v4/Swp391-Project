package org.swp391.hotelbookingsystem.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.repository.HotelRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public Hotel getHotelById(int id){
        return hotelRepository.getHotelById(id);
    }

    public List<Hotel> getHotelsSortedByRating() {
        return hotelRepository.getHotelsSortedByRating();
    }

    public List<Hotel> getTop8HighRatedHotels() {
        return hotelRepository.getTop8HighRatedHotels();
    }

    public List<Hotel> getHotelsByLocation(int id, int maxGuests, int roomQuantity, String name, int min, int max) {
        return hotelRepository.getHotelsByLocation(id, maxGuests, roomQuantity, name, min, max);
    }

    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.insertHotel(hotel);
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

    public void insertHotelDeletionToken(int userId, String token, LocalDateTime expiry, String tokenType) {
        hotelRepository.insertHotelDeletionToken(userId, token, expiry, tokenType);
    }

    public String getHotelDeleteTokenType(String token, int userId) {
        return hotelRepository.findValidTokenType(token, userId);
    }

    public void deleteHotelDeleteToken(String token, int userId) {
        hotelRepository.deleteToken(token, userId);
    }
}
