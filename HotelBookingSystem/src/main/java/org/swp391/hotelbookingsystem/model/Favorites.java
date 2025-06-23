package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorites {
    private int userId;
    private int hotelId;
    private Hotel hotel;
}