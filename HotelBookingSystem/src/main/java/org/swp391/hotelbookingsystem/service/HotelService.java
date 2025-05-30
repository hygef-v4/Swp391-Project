package org.swp391.hotelbookingsystem.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.repository.HotelRepository;

import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public List<Hotel> getHotelsSortedByRating() {
        return hotelRepository.getHotelsSortedByRating();
    }

    public List<Hotel> getTop8HighRatedHotels() {
        return hotelRepository.getTop8HighRatedHotels();
    }

    public List<Hotel> getHotelsByLocation(int id, String search) {
        if(id == -1) return hotelRepository.searchHotel(search);
        return hotelRepository.getHotelsByLocation(id, search);
    }

    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.insertHotel(hotel);
    }
}
