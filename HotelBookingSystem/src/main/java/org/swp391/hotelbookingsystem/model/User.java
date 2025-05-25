package org.swp391.hotelbookingsystem.model;

import lombok.*;


@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    private String id;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private String role;
    private boolean active;
    private String createdAt;
    private String updatedAt;


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
