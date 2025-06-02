package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Location;
import org.swp391.hotelbookingsystem.repository.LocationRepository;

import java.util.List;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.getAllLocations();
    }

    public List<Location> getLocationById(int locationId) {
        if(locationId == -1) return locationRepository.getAllLocations();
        return locationRepository.getLocation(locationId);
    }

    public List<Location> getTop5Locations() {
        List<Location> allLocations = locationRepository.getAllLocations();
        return allLocations.size() > 5 ? allLocations.subList(0, 5) : allLocations;
    }

}
