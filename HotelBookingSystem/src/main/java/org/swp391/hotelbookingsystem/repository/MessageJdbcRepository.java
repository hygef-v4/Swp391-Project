package org.swp391.hotelbookingsystem.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Message;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MessageJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public MessageJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Message save(Message message) {
        try {
            String sql = "INSERT INTO Messages (sender_id, receiver_id, content, sent_at, message_type, image_url) VALUES (?,?,?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, message.getSenderId());
                ps.setInt(2, message.getReceiverId());
                ps.setString(3, message.getContent());
                ps.setTimestamp(4, Timestamp.valueOf(message.getSentAt() == null ? LocalDateTime.now() : message.getSentAt()));
                ps.setString(5, message.getMessageType() == null ? "TEXT" : message.getMessageType());
                ps.setString(6, message.getImageUrl());
                return ps;
            }, keyHolder);
            
            if (keyHolder.getKey() != null) {
                message.setMessageId(keyHolder.getKey().intValue());
                log.debug("Message saved with ID: {}", message.getMessageId());
            }
            
            return message;
        } catch (Exception e) {
            log.error("Error saving message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save message", e);
        }
    }

    public List<Message> findConversation(int user1, int user2) {
        try {
            String sql = "SELECT * FROM Messages WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?) ORDER BY sent_at ASC";
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Message m = new Message();
                m.setMessageId(rs.getInt("message_id"));
                m.setSenderId(rs.getInt("sender_id"));
                m.setReceiverId(rs.getInt("receiver_id"));
                m.setContent(rs.getString("content"));
                
                Timestamp timestamp = rs.getTimestamp("sent_at");
                if (timestamp != null) {
                    m.setSentAt(timestamp.toLocalDateTime());
                } else {
                    m.setSentAt(LocalDateTime.now());
                }
                
                m.setMessageType(rs.getString("message_type"));
                m.setImageUrl(rs.getString("image_url"));
                return m;
            }, user1, user2, user2, user1);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No messages found for conversation between users {} and {}", user1, user2);
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching conversation between users {} and {}: {}", user1, user2, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch conversation", e);
        }
    }
} 