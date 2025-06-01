package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private int profileId;
    private int userId;
    private String avatarUrl;
    private String bio;
    private String address;
}
