package org.swp391.hotelbookingsystem.model;

import java.util.List;

import lombok.*;

@ToString
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
    private int maxGuest;
    private String status;
    private int quantity;

    private String roomType;

    private List<String> images;
}
