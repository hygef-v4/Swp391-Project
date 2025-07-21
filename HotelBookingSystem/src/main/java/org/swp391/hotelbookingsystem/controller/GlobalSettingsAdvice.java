package org.swp391.hotelbookingsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.swp391.hotelbookingsystem.model.SiteSettings;
import org.swp391.hotelbookingsystem.service.SiteSettingsService;

@ControllerAdvice
public class GlobalSettingsAdvice {
    @Autowired
    private SiteSettingsService siteSettingsService;

    @ModelAttribute("globalSettings")
    public SiteSettings globalSettings() {
        return siteSettingsService.getSettings();
    }
} 