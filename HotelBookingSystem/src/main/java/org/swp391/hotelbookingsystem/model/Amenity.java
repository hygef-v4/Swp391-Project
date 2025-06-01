package org.swp391.hotelbookingsystem.model;

import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Amenity {
    private int amenityId;
    private String name;
    private int categoryId;

    private int roomId;
    private AmenityCategory category;  // Optional for JOIN
}
