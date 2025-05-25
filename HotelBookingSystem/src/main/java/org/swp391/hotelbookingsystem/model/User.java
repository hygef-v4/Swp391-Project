package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.time.LocalDateTime;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class User {
    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
