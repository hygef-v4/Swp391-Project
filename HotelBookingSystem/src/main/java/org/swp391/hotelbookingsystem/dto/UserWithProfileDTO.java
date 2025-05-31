package org.swp391.hotelbookingsystem.dto;

import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithProfileDTO {
    private int userID;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
    private boolean isActive;
    private String avatarUrl;
    private String bio;
}
