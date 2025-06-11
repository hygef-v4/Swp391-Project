package org.swp391.hotelbookingsystem.model;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private int bookingId;
    private int hotelId;
    private int roomId;
    private int customerId;
    private Integer couponId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Double totalPrice;
    private String status;
    private Double refundAmount;
    private String refundStatus;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String roomName;
    private String hotelName;
}
