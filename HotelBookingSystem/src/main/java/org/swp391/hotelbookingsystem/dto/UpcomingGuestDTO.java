package org.swp391.hotelbookingsystem.dto;

import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpcomingGuestDTO {
    public Long id;
    public String name;
    public String room;
    public String dateRange;
    public String avatarUrl;
}
