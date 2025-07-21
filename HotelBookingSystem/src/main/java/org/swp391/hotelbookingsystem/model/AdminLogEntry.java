package org.swp391.hotelbookingsystem.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogEntry {
    private LocalDateTime timestamp;
    private String admin;
    private String action;
    private String details;
} 