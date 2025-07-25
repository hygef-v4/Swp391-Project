package org.swp391.hotelbookingsystem.service;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.AdminLogEntry;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (logs.size() > 1000) logs.remove(logs.size() - 1); // keep last 1000
    }

    public List<AdminLogEntry> getRecentLogs() {
        return logs;
    }

    public List<AdminLogEntry> getFilteredLogs(String adminFilter, String actionFilter,
                                               int page, int size) {
        List<AdminLogEntry> filteredLogs = logs.stream()
            .filter(log -> {
                // Admin filter
                if (adminFilter != null && !adminFilter.trim().isEmpty()) {
                    if (!log.getAdmin().toLowerCase().contains(adminFilter.toLowerCase())) {
                        return false;
                    }
                }

                // Action filter
                if (actionFilter != null && !actionFilter.trim().isEmpty()) {
                    if (!log.getAction().toLowerCase().contains(actionFilter.toLowerCase())) {
                        return false;
                    }
                }


                return true;
            })
            .collect(Collectors.toList());

        // Pagination
        int start = (page - 1) * size;
        int end = Math.min(start + size, filteredLogs.size());

        if (start >= filteredLogs.size()) {
            return List.of();
        }

        return filteredLogs.subList(start, end);
    }

    public int getTotalFilteredCount(String adminFilter, String actionFilter) {
        return (int) logs.stream()
            .filter(log -> {
                // Admin filter
                if (adminFilter != null && !adminFilter.trim().isEmpty()) {
                    if (!log.getAdmin().toLowerCase().contains(adminFilter.toLowerCase())) {
                        return false;
                    }
                }

                // Action filter
                if (actionFilter != null && !actionFilter.trim().isEmpty()) {
                    if (!log.getAction().toLowerCase().contains(actionFilter.toLowerCase())) {
                        return false;
                    }
                }

                return true;
            })
            .count();
    }

    public List<String> getUniqueAdmins() {
        return logs.stream()
            .map(AdminLogEntry::getAdmin)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public List<String> getUniqueActions() {
        return logs.stream()
            .map(AdminLogEntry::getAction)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}