package org.swp391.hotelbookingsystem.model;

import java.util.List;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private int roomId;
    private int hotelId;
    private String title;
    private String description;
    private float price;
    private int maxGuests;
    private String status;
    private int quantity;
    private int roomTypeId;

    private String roomType;

    private List<String> images;
    private List<Amenity> amenities;
    private List<AmenityCategory> categories;

    private int bookedCount;

}
