package org.swp391.hotelbookingsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String buildUrl(String path) {
        if (path.startsWith("/")) {
            return baseUrl + path;
        }
        return baseUrl + "/" + path;
    }
} 