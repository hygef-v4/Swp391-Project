package org.swp391.hotelbookingsystem.repository;

import org.swp391.hotelbookingsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserRepo {
    @Autowired
    private JdbcTemplate jdbc;

    public UserRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO Users (full_name,email, password_hash) VALUES (?, ?, ?)";
        jdbc.update(sql, user.getFullname(), user.getEmail(), user.getPassword());
    }
    public void saveUserFromGoogle(User user) {
        String sql = "INSERT INTO Users (full_name, email) VALUES (?, ?)";
        jdbc.update(sql, user.getFullname(), user.getEmail());
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setFullname(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                return user;
            }, email);
        } catch (Exception e) {
            return null;
        }
    }

    private static String SELECT_USERS_BY_ROLE = """
            SELECT u.user_id AS userID, 
                   u.full_name AS fullName, 
                   u.email, 
                   u.password_hash AS password, 
                   u.phone, 
                   u.role, 
                   u.is_active
            FROM Users u WHERE role = ?
            """;

    private static String SELECT_USERS_WITH_PROFILE = """
            SELECT u.user_id AS userID, 
                   u.full_name AS fullName, 
                   u.email, 
                   u.password_hash AS password, 
                   u.phone, 
                   u.role, 
                   u.is_active,
                   up.avatar_url AS avatarUrl,
                   up.bio
            FROM Users u
            JOIN UserProfiles up ON u.user_id = up.user_id
            """;


    public List<User> getAllUsersWithProfile() {
        return jdbc.query(SELECT_USERS_WITH_PROFILE, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("userID"));
            user.setFullname(rs.getString("fullName"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            user.setActive(rs.getBoolean("is_active"));
            user.setAvatarUrl(rs.getString("avatarUrl"));
            user.setBio(rs.getString("bio"));
            return user;
        });
    }



    public List<User> getUsersByRole(String role) {
        return jdbc.query(SELECT_USERS_BY_ROLE, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("userID"));
            user.setFullname(rs.getString("fullName"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            user.setActive(rs.getBoolean("is_active"));
            return user;
        }, role);
    }


    public List<User> getAllUser() {
        String sql = "SELECT * FROM Users";
        return jdbc.query(sql, (rs, rowNum) -> new User(
                rs.getString("email"),
                rs.getString("password_hash")
        ));
    }

    public void savePasswordResetToken(int userId, String token) {
        String sql = "INSERT INTO Tokens (user_id, token, expiry_date, token_type) VALUES (?, ?, DATEADD(MINUTE, 30, GETDATE()), 'reset password')";
        jdbc.update(sql, userId, token);
    }

    // Tìm user thông qua token còn hiệu lực
    public User findUserByToken(String token) {
        String sql = """
            SELECT u.* FROM Users u
            JOIN Tokens t ON u.user_id = t.user_id
            WHERE t.token = ? AND t.expiry_date > GETDATE()
        """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setFullname(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                return user;
            }, token);
        } catch (Exception e) {
            return null;
        }
    }

    // Xóa token đã dùng hoặc hết hạn
    public void deleteToken(String token) {
        String sql = "DELETE FROM Tokens WHERE token = ?";
        jdbc.update(sql, token);
    }

    public void saveEmailOtpToken(String email, String otp) {
        String sql = """
            INSERT INTO Tokens (user_id, token, expiry_date, token_type)
            VALUES ((SELECT user_id FROM Users WHERE email = ?), ?, ?, 'email verify')
        """;
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        jdbc.update(sql, email, otp, Timestamp.valueOf(expiry));
    }

    public boolean isValidEmailOtp(String email, String otp) {
        String sql = """
            SELECT COUNT(*) FROM Tokens
            WHERE token = ? AND token_type = 'email verify'
            AND expiry_date > GETDATE()
            AND (user_id = (SELECT user_id FROM Users WHERE email = ?) OR user_id IS NULL)
        """;
        Integer count = jdbc.queryForObject(sql, Integer.class, otp, email);
        return count != null && count > 0;
    }

    public void deleteEmailOtp(String email, String otp) {
        String sql = """
            DELETE FROM Tokens
            WHERE token = ? AND token_type = 'email verify'
            AND (user_id = (SELECT user_id FROM Users WHERE email = ?) OR user_id IS NULL)
        """;
        jdbc.update(sql, otp, email);
    }

    // Cập nhật mật khẩu mới
    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE Users SET password_hash = ? WHERE user_id = ?";
        jdbc.update(sql, hashedPassword, userId);
    }

    public void updateUserPassword(String email, String encodedPassword) {
        String sql = "UPDATE Users SET password_hash = ? WHERE email = ?";
        jdbc.update(sql, encodedPassword, email);
    }

    public void updateUserRoleById(int userId, String newRole) {
        String sql = "UPDATE Users SET role = ? WHERE user_id = ?";
        jdbc.update(sql, newRole, userId);
    }

    public void updateUser(User user) {
        String sql = "UPDATE Users SET full_name = ?, phone = ? WHERE email = ?";
        jdbc.update(sql, user.getFullname(), user.getPhone(), user.getEmail());
    }

    public List<User> searchUsersWithProfileByName(String search) {
        String sql = """
        SELECT u.user_id AS userID,
               u.full_name AS fullName,
               u.email,
               u.password_hash AS password,
               u.phone,
               u.role,
               u.is_active,
               up.avatar_url AS avatarUrl,
               up.bio
        FROM Users u
        LEFT JOIN UserProfiles up ON u.user_id = up.user_id
        WHERE u.full_name LIKE ? 
        """;

        String wildcard = "%" + search + "%";
        return jdbc.query(sql, new Object[]{wildcard}, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("userID"));
            user.setFullname(rs.getString("fullName"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            user.setActive(rs.getBoolean("is_active"));
            user.setAvatarUrl(rs.getString("avatarUrl"));
            user.setBio(rs.getString("bio"));
            return user;
        });
    }

    public User findUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setFullname(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                return user;
            }, userId);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE Users SET is_active = ? WHERE user_id = ?";
        jdbc.update(sql, isActive, userId);
    }



}


