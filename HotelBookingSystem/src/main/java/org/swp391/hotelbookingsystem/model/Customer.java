package org.swp391.hotelbookingsystem.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private User user;
    private long totalBookings;
    private long activeBookings;
    private long completedBookings;
    private double totalSpent;
    private List<Booking> bookings;
} 