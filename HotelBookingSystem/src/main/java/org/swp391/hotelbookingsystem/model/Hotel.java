package org.swp391.hotelbookingsystem.model;

import lombok.*;

@ToString
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

    private String cityName; // Added field for city name
}
