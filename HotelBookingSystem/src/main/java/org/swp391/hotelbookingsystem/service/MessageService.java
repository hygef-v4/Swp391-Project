package org.swp391.hotelbookingsystem.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Message;
import org.swp391.hotelbookingsystem.repository.MessageJdbcRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageJdbcRepository messageRepo;

    public Message save(Message message) {
        return messageRepo.save(message);
    }

    public List<Message> getConversation(int user1, int user2) {
        return messageRepo.findConversation(user1, user2);
    }
} 