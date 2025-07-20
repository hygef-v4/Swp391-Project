package org.swp391.hotelbookingsystem.controller.chatbot;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SessionContextCache {
    private final Map<String, List<String>> sessionHistories = new HashMap<>();
    private final Map<String, String> userNames = new HashMap<>();

    public void addMessage(String sessionId, String message) {
        sessionHistories.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        if (sessionHistories.get(sessionId).size() > 10) {
            sessionHistories.get(sessionId).remove(0);
        }
    }

    public List<String> getSessionHistory(String sessionId) {
        return sessionHistories.getOrDefault(sessionId, new ArrayList<>());
    }

    public void setUserName(String sessionId, String name) {
        userNames.put(sessionId, name);
    }

    public String getUserName(String sessionId) {
        return userNames.getOrDefault(sessionId, "bạn");
    }
}
