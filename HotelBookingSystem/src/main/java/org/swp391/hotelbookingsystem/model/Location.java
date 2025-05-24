package org.swp391.hotelbookingsystem.model;
import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter


public class Location {
    private int id;
    private String cityName;
    private String imageUrl;
}