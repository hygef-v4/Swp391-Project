package org.swp391.hotelbookingsystem.controller.chat;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.model.Message;
import org.swp391.hotelbookingsystem.service.CloudinaryService;
import org.swp391.hotelbookingsystem.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {

    private final MessageService messageService;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/chat/history")
    public ResponseEntity<List<Message>> getChatHistory(@RequestParam Integer user1, @RequestParam Integer user2) {
        try {
            log.info("Fetching chat history for users: {} and {}", user1, user2);
            List<Message> messages = messageService.getConversation(user1, user2);
            log.info("Retrieved {} messages", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching chat history for users {} and {}: {}", user1, user2, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PostMapping("/api/chat/upload-image")
    public ResponseEntity<Map<String,String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading image: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                log.warn("Received empty file upload request");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type: {}", contentType);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be an image"));
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.warn("File too large: {} bytes", file.getSize());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size must be less than 5MB"));
            }
            
            String url = (String) cloudinaryService.uploadImage(file, "chat").get("secure_url");
            log.info("Image uploaded successfully: {}", url);
            return ResponseEntity.ok(Map.of("url", url));
            
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @MessageMapping("/presence/{roomId}")
    public void handlePresence(@DestinationVariable String roomId, @Payload Map<String, Object> presence) {
        log.info("Received presence update for room {}: {}", roomId, presence);
        
        try {
            // Broadcast presence update to all subscribers of this room
            messagingTemplate.convertAndSend("/topic/presence." + roomId, presence);
            log.info("Broadcasted presence update to /topic/presence.{}", roomId);
        } catch (Exception e) {
            log.error("Error broadcasting presence for room {}: {}", roomId, e.getMessage(), e);
        }
    }
} 