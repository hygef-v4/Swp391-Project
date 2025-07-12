package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.sql.Date;

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
    private Date dob;
    // Xoá isFlagged và flagReason khỏi model
    // private boolean isFlagged = false;
    // private String flagReason;
    // Thêm getter/setter tạm thời cho flagged/flagReason để phục vụ view (không lưu DB)
    @lombok.Getter @lombok.Setter
    private transient boolean flagged;
    @lombok.Getter @lombok.Setter
    private transient String flagReason;

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
