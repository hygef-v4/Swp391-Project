package org.swp391.hotelbookingsystem.model;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private int id;
    private String cityName;
    private String imageUrl;
}