package org.swp391.hotelbookingsystem.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String status;
    private List<BookingUnit> bookingUnits;
    private String customerName;
    private String customerEmail;
    private String customerAvatar;

    public int getBookingUnitSize(){
        int size = 0;
        for(BookingUnit bookingUnit : bookingUnits){
            size += bookingUnit.getQuantity();
        }return size;
    }
}
