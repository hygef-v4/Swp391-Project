package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.dto.*;
import org.swp391.hotelbookingsystem.model.*;
import org.swp391.hotelbookingsystem.repository.AdminDashboardRepository;

import java.util.*;

@Service
public class AdminDashboardService {

    @Autowired
    private AdminDashboardRepository repository;

    public long countHotels() {
        return repository.countHotels();
    }

    public long countRooms() {
        return repository.countRooms();
    }

    public long countBookedRooms() {
        return repository.countBookedRooms();
    }

    public long countAvailableRooms() {
        return repository.countAvailableRooms();
    }

    public List<Hotel> getTopPopularHotels() {
        return repository.findTopPopularHotels(); // Assume it sorts by booking count or rating
    }

    public List<RecentRoomBookingDTO> getRecentRoomBookings() {
        return repository.findRecentRoomBookings();
    }

    public List<UpcomingGuestDTO> getUpcomingGuests() {
        return repository.findUpcomingGuests();
    }

    public List<ReviewDTO> getRecentReviews() {
        return repository.findRecentReviews();
    }

    public Map<String, Integer> getCheckInChartDataByDate() {
        return repository.getCheckInCountsByDate();
    }

    public Map<String, Integer> getCheckOutChartDataByDate() {
        return repository.getCheckOutCountsByDate();
    }

}
