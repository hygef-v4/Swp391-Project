package org.swp391.hotelbookingsystem.service;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.AdminLogEntry;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
public class AdminLogService {
    private final List<AdminLogEntry> logs = new LinkedList<>();

    public void log(String admin, String action, String details) {
        AdminLogEntry entry = new AdminLogEntry();
        entry.setTimestamp(LocalDateTime.now());
        entry.setAdmin(admin);
        entry.setAction(action);
        entry.setDetails(details);
        logs.add(0, entry); // newest first
        if (logs.size() > 100) logs.remove(logs.size() - 1); // keep last 100
    }

    public List<AdminLogEntry> getRecentLogs() {
        return logs;
    }
} 