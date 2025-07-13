package org.swp391.hotelbookingsystem.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {
    private int reportId;
    private int reporterId;
    private int reportedUserId;
    private String reason;
    private String status; // pending, accepted, declined
    private LocalDateTime createdAt;
} 