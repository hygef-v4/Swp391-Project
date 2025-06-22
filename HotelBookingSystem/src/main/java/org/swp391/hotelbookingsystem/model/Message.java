package org.swp391.hotelbookingsystem.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private LocalDateTime sentAt;
    private String messageType; // TEXT, IMAGE, etc.
    private String imageUrl;
} 