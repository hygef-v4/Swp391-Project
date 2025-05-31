package org.swp391.hotelbookingsystem.model;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private int bookingId;
    private int roomId;
    private int customerId;
    private Integer couponId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private double totalPrice;
    private String status;
    private LocalDateTime createdAt;
}
