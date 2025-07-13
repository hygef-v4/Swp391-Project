package org.swp391.hotelbookingsystem.model;

import java.time.LocalDateTime;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reply{
    private int replyId;
    private int reviewId;
    private int replierId;
    private String comment;
    private boolean isPublic;
    private LocalDateTime createdAt;

    // for User
    private int userId;
    private String fullName;
    private String avatarUrl;
}
