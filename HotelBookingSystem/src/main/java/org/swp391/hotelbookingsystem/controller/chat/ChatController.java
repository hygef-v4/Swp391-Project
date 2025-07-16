package org.swp391.hotelbookingsystem.controller.chat;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.swp391.hotelbookingsystem.dto.ChatMessage;
import org.swp391.hotelbookingsystem.model.Message;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.MessageService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import org.swp391.hotelbookingsystem.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final UserService userService;

    // client sends to /app/chat/{roomId}
    @MessageMapping("/chat/{roomId}")
    public void processMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        try {
            log.info("Processing message for room: {} from user: {}", roomId, chatMessage.getSenderId());
            
            // Validate required fields
            if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null) {
                log.error("Invalid message: missing sender or receiver ID");
                return;
            }
            
            // save to db
            Message msg = new Message();
            msg.setSenderId(chatMessage.getSenderId());
            msg.setReceiverId(chatMessage.getReceiverId());
            msg.setContent(chatMessage.getContent() != null ? chatMessage.getContent() : "");
            msg.setMessageType(chatMessage.getMessageType() == null ? "TEXT" : chatMessage.getMessageType());
            msg.setImageUrl(chatMessage.getImageUrl());
            msg.setSentAt(LocalDateTime.now());
            
            Message savedMessage = messageService.save(msg);
            log.info("Message saved with ID: {}", savedMessage.getMessageId());

            chatMessage.setSentAt(savedMessage.getSentAt());
            
            // send to topic
            messagingTemplate.convertAndSend("/topic/chat." + roomId, chatMessage);
            log.info("Message sent to topic: /topic/chat.{}", roomId);
            
            // Create notification for the receiver
            try {
                User sender = userService.findUserById(chatMessage.getSenderId());
                User receiver = userService.findUserById(chatMessage.getReceiverId());
                
                if (sender != null && receiver != null) {
                    String senderName = sender.getFullName();
                    
                    // Determine the correct action URL based on receiver's role
                    String actionUrl;
                    if (receiver.getRole().equals("HOTEL_OWNER")) {
                        // Host receives message from customer
                        actionUrl = "/host-customer-detail?customerId=" + chatMessage.getSenderId();
                    } else {
                        // Customer receives message from host  
                        actionUrl = "/customer-host-detail?hostId=" + chatMessage.getSenderId();
                    }
                    
                    // Use enhanced notification method that merges notifications from same sender
                    notificationService.createOrUpdateChatNotification(
                        chatMessage.getReceiverId(),
                        senderName,
                        chatMessage.getSenderId(),
                        chatMessage.getContent(),
                        actionUrl
                    );
                    
                    log.info("Chat notification created/updated for user {} from sender {}", 
                            chatMessage.getReceiverId(), chatMessage.getSenderId());
                } else {
                    log.warn("Could not find sender {} or receiver {} for notification", 
                            chatMessage.getSenderId(), chatMessage.getReceiverId());
                }
            } catch (Exception notificationError) {
                log.error("Error creating/updating chat notification: {}", notificationError.getMessage(), notificationError);
                // Don't fail the message sending if notification fails
            }
            
        } catch (Exception e) {
            log.error("Error processing message for room {}: {}", roomId, e.getMessage(), e);
            // Optionally send error message back to client
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("Lỗi server: Không thể gửi tin nhắn");
            errorMessage.setMessageType("ERROR");
            errorMessage.setSentAt(LocalDateTime.now());
            messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId().toString(), 
                "/queue/errors", 
                errorMessage
            );
        }
    }
} 