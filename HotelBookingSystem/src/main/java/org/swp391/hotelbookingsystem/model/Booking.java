package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private int bookingId;
    private int hotelId;
    private int customerId;
    private Integer couponId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Double totalPrice;
    private LocalDateTime createdAt;
    
    private String hotelName;
    private String imageUrl;
    private List<BookingUnit> bookingUnits;
}
