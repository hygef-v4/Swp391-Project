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

    public String determineStatus() {
        boolean hasCheckIn = false;
        boolean hasCompleted = false;
        boolean hasApproved = false;
        boolean allCancelledOrRejected = true;

        for (BookingUnit unit : bookingUnits) {
            String status = unit.getStatus();
            if ("check_in".equals(status)) {
                hasCheckIn = true;
            } else if ("completed".equals(status)) {
                hasCompleted = true;
            } else if ("approved".equals(status)) {
                hasApproved = true;
            }

            if (!"cancelled".equals(status) && !"rejected".equals(status)) {
                allCancelledOrRejected = false;
            }
        }

        if (hasCheckIn) return "check_in";
        if (hasCompleted) return "completed";
        if (hasApproved) return "approved";
        if (allCancelledOrRejected) return "cancelled";
        return null;
    }

    public double calculateTotalPrice() {
        if (bookingUnits == null) return 0.0;

        return bookingUnits.stream()
                .filter(unit -> {
                    String status = unit.getStatus();
                    return "approved".equals(status)
                            || "completed".equals(status)
                            || "check_in".equals(status);
                })
                .mapToDouble(unit -> unit.getPrice() * unit.getQuantity())
                .sum();
    }

}
