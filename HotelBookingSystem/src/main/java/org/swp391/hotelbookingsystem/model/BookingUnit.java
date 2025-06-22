package org.swp391.hotelbookingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int roomCapacity;
    
    private String imageUrl;
    private String roomName;
    private boolean cancelable;
}
