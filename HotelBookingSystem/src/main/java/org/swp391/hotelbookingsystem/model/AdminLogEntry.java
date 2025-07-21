package org.swp391.hotelbookingsystem.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AdminLogEntry {
    private LocalDateTime timestamp;
    private String admin;
    private String action;
    private String details;

    public Map<String, String> getDetailsMap() {
        Map<String, String> map = new LinkedHashMap<>();
        if (details != null && details.startsWith("SiteSettings(") && details.endsWith(")")) {
            String content = details.substring("SiteSettings(".length(), details.length() - 1);
            String[] fields = content.split(", ");
            for (String field : fields) {
                String[] kv = field.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }
} 