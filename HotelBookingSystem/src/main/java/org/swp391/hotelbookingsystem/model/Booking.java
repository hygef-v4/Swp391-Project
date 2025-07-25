package org.swp391.hotelbookingsystem.model;

import java.time.LocalDateTime;
import java.util.List;
import java.time.temporal.ChronoUnit;


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
    private String orderCode;
    private String transactionNo;
    private LocalDateTime createdAt;

    private int partialRefundDay;
    private int partialRefundPercent;
    private int noRefund;
    
    private int hostId;
    private String hotelName;
    private String imageUrl;
    private String status;
    private List<BookingUnit> bookingUnits;
    private String customerName;
    private String customerEmail;
    private String customerAvatar;
    private long numberOfNights;

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
        boolean allRejected = true;
        boolean allCancelled = true;

        for (BookingUnit unit : bookingUnits) {
            String status = unit.getStatus();

            switch (status) {
                case "check_in" -> hasCheckIn = true;
                case "completed" -> hasCompleted = true;
                case "approved" -> hasApproved = true;
            }

            if (!"rejected".equals(status)) {
                allRejected = false;
            }

            if (!"cancelled".equals(status)) {
                allCancelled = false;
            }
        }

        if (hasCheckIn) return "check_in";
        if (hasCompleted) return "completed";
        if (hasApproved) return "approved";
        if (allRejected) return "rejected";
        if (allCancelled) return "cancelled";
        return "cancelled"; // fallback for mix of rejected + cancelled
    }

    public void calculateNumberOfNights() {
        if (checkIn != null && checkOut != null) {
            long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
            this.numberOfNights = (nights == 0) ? 1 : nights;
        } else {
            this.numberOfNights = 0;
        }
    }

    public double calculateTotalPrice() {
        calculateNumberOfNights();
        if (bookingUnits == null || numberOfNights == 0) return 0.0;

        return bookingUnits.stream()
//                .filter(unit -> {
//                    String status = unit.getStatus();
//                    return "approved".equals(status)
//                            || "completed".equals(status)
//                            || "check_in".equals(status);
//                })
                .mapToDouble(unit -> unit.getPrice() * unit.getQuantity() * numberOfNights)
                .sum();
    }

    public long refundAmount(){
        long day = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), checkIn.toLocalDate());

        if(day <= noRefund) return 0;
        if(day <= partialRefundDay) return (long)(totalPrice.longValue() * ((float)partialRefundPercent / 100));
        return totalPrice.longValue();
    }

    /**
     * Calculate refund amount using a specific total price (useful when totalPrice has been modified)
     * @param originalTotalPrice The original total price paid by customer
     * @return Refund amount based on cancellation policy
     */
    public long refundAmountWithOriginalPrice(double originalTotalPrice){
        long day = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), checkIn.toLocalDate());

        if(day <= noRefund) {
            return 0;
        }
        if(day <= partialRefundDay) {
            long partialRefund = (long)(originalTotalPrice * ((float)partialRefundPercent / 100));
            return partialRefund;
        }
        return (long)originalTotalPrice;
    }

    /**
     * Calculate refund amount for host-initiated cancellations (always 100% refund)
     * @param originalTotalPrice The original total price paid by customer
     * @return Full refund amount (100% of original price)
     */
    public long hostInitiatedRefundAmount(double originalTotalPrice){
        System.out.println("=== HOST-INITIATED REFUND ===");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Original Total Price: " + originalTotalPrice);
        System.out.println("RESULT: Full refund (100%) = " + (long)originalTotalPrice);
        return (long)originalTotalPrice;
    }
}
