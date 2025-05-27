package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.UserProfile;

@Repository
public class UserProfileRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public UserProfile findByUserId(int userId) {
        String sql = "SELECT * FROM UserProfiles WHERE user_id = ?";
        return jdbc.queryForObject(sql, new Object[]{userId}, (rs, rowNum) -> {
            UserProfile profile = new UserProfile();
            profile.setProfileId(rs.getInt("profile_id"));
            profile.setUserId(rs.getInt("user_id"));
            profile.setAvatarUrl(rs.getString("avatar_url"));
            profile.setBio(rs.getString("bio"));
            profile.setAddress(rs.getString("address"));
            return profile;
        });
    }

    public void updateUserProfile(UserProfile profile) {
        String sql = "UPDATE UserProfiles SET avatar_url = ?, bio = ?, address = ? WHERE user_id = ?";
        jdbc.update(sql, profile.getAvatarUrl(), profile.getBio(), profile.getAddress(), profile.getUserId());
    }
}