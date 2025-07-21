package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.SiteSettings;
import org.swp391.hotelbookingsystem.repository.SiteSettingsRepository;

@Service
public class SiteSettingsService {
    @Autowired
    private SiteSettingsRepository repo;

    public SiteSettings getSettings() {
        return repo.getSettings();
    }

    public void updateSettings(SiteSettings s) {
        repo.updateSettings(s);
    }
} 