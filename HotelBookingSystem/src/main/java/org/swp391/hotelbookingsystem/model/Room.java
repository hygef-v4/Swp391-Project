package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private int roomId;
    private int hotelId;
    private String title;
    private String description;
    private BigDecimal price;
    private int maxGuests;
    private int roomTypeId;
    private String status;
    private int quantity;
}
