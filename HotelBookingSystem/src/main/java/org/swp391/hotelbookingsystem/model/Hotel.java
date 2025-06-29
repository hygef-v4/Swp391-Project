package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {
    private int hotelId;
    private int hostId;
    private String hotelName;
    private String address;
    private String description;
    private int locationId;
    private String hotelImageUrl;
    private double rating;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String policy;
    private String status;

    private String cityName;
    private BigDecimal minPrice;
    private int maxGuests;
    private int roomQuantity;
    private String hostName;
}
