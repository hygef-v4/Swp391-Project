package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypes {
    private int roomTypeId;
    private String name;
    private String description;
}
