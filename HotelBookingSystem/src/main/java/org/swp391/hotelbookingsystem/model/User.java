package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
    private boolean active;
    private String gender;
    private String avatarUrl;
    private String bio;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
