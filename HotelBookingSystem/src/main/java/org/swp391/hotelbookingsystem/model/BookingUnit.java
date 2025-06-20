package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingUnit {
    private int bookingUnitId;
    private int roomId;
    private Double price;
    private String status;
    private Double refundAmount;
    private String refundStatus;
    private int quantity;
    
    private String imageUrl;
    private String roomName;
    private boolean cancelable;
}
