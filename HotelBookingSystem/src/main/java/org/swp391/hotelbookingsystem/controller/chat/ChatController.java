package org.swp391.hotelbookingsystem.controller.chat;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.swp391.hotelbookingsystem.dto.ChatMessage;
import org.swp391.hotelbookingsystem.model.Message;
import org.swp391.hotelbookingsystem.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

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