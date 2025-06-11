package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.repository.AmenityRepository;

import java.util.List;

@Service
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    public List<Amenity> getAllAmenities() {
        return amenityRepository.getAllAmenities();
    }

    public List<Amenity> getAmenitiesByCategoryId(int categoryId) {
        return amenityRepository.getAmenitiesByCategoryId(categoryId);
    }

    public List<Amenity> getAllAmenitiesWithCategory() {
        return amenityRepository.getAllAmenitiesWithCategory();
    }

    public List<Amenity> getRoomAmenities(int roomId){
        return amenityRepository.getRoomAmenities(roomId);
    }
}
