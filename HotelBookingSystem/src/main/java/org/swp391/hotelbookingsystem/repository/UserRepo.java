package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.User;


import java.util.List;

@Repository
public class UserRepo {
    @Autowired
    private JdbcTemplate jdbc;

    public UserRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO Users (email, password_hash) VALUES (?, ?)";
        jdbc.update(sql, user.getEmail(), user.getPassword());
    }
    public void saveUserFromGoogle(User user) {
        String sql = "INSERT INTO Users (email) VALUES (?)";
        jdbc.update(sql, user.getEmail());
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getString("user_id"));
                user.setFullname(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash")); // map đúng
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getString("created_at"));
                user.setUpdatedAt(rs.getString("updated_at"));
                return user;
            }, email);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<User> getAllUser() {
        String sql = "SELECT * FROM Users";
        return jdbc.query(sql, (rs, rowNum) -> new User(
                rs.getString("email"),
                rs.getString("password_hash")
        ));
    }
}
