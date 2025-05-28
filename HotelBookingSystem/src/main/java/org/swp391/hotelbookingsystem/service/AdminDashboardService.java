package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.repository.AdminDashboardRepository;
import org.swp391.hotelbookingsystem.repository.HotelRepository;

import java.util.List;

@Service
public class AdminDashboardService {
    @Autowired
    private AdminDashboardRepository adminDashboardRepository;

    public int getNumberOfHotels() {
        return adminDashboardRepository.getNumberOfHotels();
    }

    public int getTotalRooms() {
        return adminDashboardRepository.getTotalRooms();
    }

    public int getTotalBookedRooms() {
        return adminDashboardRepository.getTotalBookedRooms();
    }

    public List<Hotel> getTopPopularHotels() {
        return adminDashboardRepository.getTopPopularHotels();
    }

    public List<Integer> getCheckInChartData() {
        return adminDashboardRepository.getCheckInCountsByWeekday();
    }

    public List<Integer> getCheckOutChartData() {
        return adminDashboardRepository.getCheckOutCountsByWeekday();
    }

}
