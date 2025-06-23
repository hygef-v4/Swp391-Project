package org.swp391.hotelbookingsystem.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatMessage {
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String messageType; // TEXT, IMAGE
    private String imageUrl;
    private LocalDateTime sentAt;
} 