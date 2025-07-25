package org.swp391.hotelbookingsystem.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AdminLogEntry {
    private LocalDateTime timestamp;
    private String admin;
    private String action;
    private String details;

} 