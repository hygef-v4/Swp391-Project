package org.swp391.hotelbookingsystem.model;

import java.util.List;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmenityCategory {
    private int categoryId;
    private String name;

    private List<Amenity> amenities;
}
