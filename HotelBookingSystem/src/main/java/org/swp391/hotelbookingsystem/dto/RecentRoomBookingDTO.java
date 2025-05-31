package org.swp391.hotelbookingsystem.dto;

/**
 * Data Transfer Object for recent room bookings.
 * This class is used to transfer data related to recent room bookings
 * between different layers of the application.
 */
import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecentRoomBookingDTO {
    public Long id;
    public String name;
    public String dateRange;
    public String status;
    public String imageUrl;
}
