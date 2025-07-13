package org.swp391.hotelbookingsystem.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private int reviewId;
    private int hotelId;
    private int reviewerId;
    private int rating;
    private String comment;
    private boolean isPublic;
    private LocalDateTime createdAt;

    // for User
    private int userId;
    private String fullName;
    private String avatarUrl;
    private String bio;

    private String hotelName;

    private List<Reply> replies;
}
