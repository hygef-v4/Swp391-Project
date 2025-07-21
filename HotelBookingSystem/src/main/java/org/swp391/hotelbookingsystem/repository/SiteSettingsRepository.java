package org.swp391.hotelbookingsystem.repository;

import org.swp391.hotelbookingsystem.model.SiteSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SiteSettingsRepository {
    private final JdbcTemplate jdbc;

    public SiteSettingsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SiteSettings getSettings() {
        String sql = "SELECT * FROM SiteSettings WHERE id = 1";
        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            SiteSettings s = new SiteSettings();
            s.setSiteName(rs.getString("site_name"));
            s.setSupportEmail(rs.getString("support_email"));
            s.setContactPhone(rs.getString("contact_phone"));
            s.setContactAddress(rs.getString("contact_address"));
            return s;
        });
    }

    public void updateSettings(SiteSettings s) {
        String sql = "UPDATE SiteSettings SET site_name=?, support_email=?, contact_phone=?, contact_address=? WHERE id=1";
        jdbc.update(sql, s.getSiteName(), s.getSupportEmail(), s.getContactPhone(), s.getContactAddress());
    }
} 