package org.swp391.hotelbookingsystem.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@Repository
public class UserRepo {

    private JdbcTemplate jdbc;

    public UserRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> getAllUser() {
        String sql = "SELECT * FROM Users";
        return jdbc.query(sql, (rs, rowNum) -> new User(
                rs.getString("email"),
                rs.getString("password_hash")
        ));
    }

    public int countAgent() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'HOTEL_OWNER'";
        return jdbc.queryForObject(sql, Integer.class);
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO Users (full_name,email, password_hash) VALUES (?, ?, ?)";
        jdbc.update(sql, user.getFullName(), user.getEmail(), user.getPassword());
    }
    public void saveUserFromGoogle(User user) {
        String sql = "INSERT INTO Users (full_name, email) VALUES (?, ?)";
        jdbc.update(sql, user.getFullName(), user.getEmail());
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> User.builder()
                    .id(rs.getInt("user_id"))
                    .fullName(rs.getString("full_name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password_hash"))
                    .phone(rs.getString("phone"))
                    .role(rs.getString("role"))
                    .active(rs.getBoolean("is_active"))
                    .dob(rs.getDate("date_of_birth"))
                    .bio(rs.getString("bio"))
                    .gender(rs.getString("gender"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .build(), email);
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getUsersByRole(String role) {
        String sql = "SELECT * FROM Users WHERE role = ?";
        try {
            return jdbc.query(sql, (rs, rowNum) -> User.builder()
                    .id(rs.getInt("user_id"))
                    .fullName(rs.getString("full_name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password_hash"))
                    .phone(rs.getString("phone"))
                    .role(rs.getString("role"))
                    .active(rs.getBoolean("is_active"))
                    .dob(rs.getDate("date_of_birth"))
                    .bio(rs.getString("bio"))
                    .gender(rs.getString("gender"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .build(), role);
        } catch (Exception e) {
            return null;
        }
    }

    public void savePasswordResetToken(int userId, String token) {
        String sql = "INSERT INTO Tokens (user_id, token, expiry_date, token_type) VALUES (?, ?, DATEADD(MINUTE, 30, GETDATE()), 'reset password')";
        jdbc.update(sql, userId, token);
    }

    public User findUserByToken(String token) {
        String sql = """
            SELECT u.* FROM Users u
            JOIN Tokens t ON u.user_id = t.user_id
            WHERE t.token = ? AND t.expiry_date > GETDATE()
        """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> User.builder()
                    .id(rs.getInt("user_id"))
                    .fullName(rs.getString("full_name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password_hash"))
                    .phone(rs.getString("phone"))
                    .role(rs.getString("role"))
                    .active(rs.getBoolean("is_active"))
                    .build(), token);
        } catch (Exception e) {
            return null;
        }
    }

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

    public void deleteEmailOtp(String email) {
        String sql = """
            DELETE FROM Tokens
            WHERE token_type = 'email verify'
            AND (user_id = (SELECT user_id FROM Users WHERE email = ?) OR user_id IS NULL)
        """;
        jdbc.update(sql, email);
    }

    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE Users SET password_hash = ? WHERE user_id = ?";
        jdbc.update(sql, hashedPassword, userId);
    }

    private static String SELECT_USERS_WITH_PROFILE = """
            SELECT u.user_id AS userID,
                   u.full_name AS fullName,
                   u.email,
                   u.password_hash AS password,
                   u.phone,
                   u.role,
                   u.is_active,
                   u.avatar_url AS avatarUrl,
                   u.bio,
                   u.gender,
                   u.date_of_birth,
                   u.is_flagged,
                   u.flag_reason
            FROM Users u
            """;



    public List<User> getAllUsersWithProfile() {
        String sql = SELECT_USERS_WITH_PROFILE + " ORDER BY u.user_id";
        return jdbc.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("userID"));
            user.setFullName(rs.getString("fullName"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            user.setActive(rs.getBoolean("is_active"));
            user.setAvatarUrl(rs.getString("avatarUrl"));
            user.setBio(rs.getString("bio"));
            user.setGender(rs.getString("gender"));
            user.setDob(rs.getDate("date_of_birth"));
            user.setFlagged(rs.getBoolean("is_flagged"));
            user.setFlagReason(rs.getString("flag_reason"));
            return user;
        });
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
        String sql = """
                      UPDATE Users 
                      SET full_name = ?, phone = ?, gender = ?, bio = ?, date_of_birth = ? 
                      WHERE email = ?
                     """;
        jdbc.update(sql, user.getFullName(), user.getPhone(), user.getGender(), user.getBio(), user.getDob(), user.getEmail());
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
               u.avatar_url AS avatarUrl,
               u.bio
        FROM Users u
        WHERE u.full_name LIKE ? 
        """;

        String wildcard = "%" + search + "%";
        return jdbc.query(sql, new Object[]{wildcard}, (rs, rowNum) -> User.builder()
                .id(rs.getInt("userID"))
                .fullName(rs.getString("fullName"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .avatarUrl(rs.getString("avatarUrl"))
                .bio(rs.getString("bio"))
                .build());
    }

    public User findUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> User.builder()
                    .id(rs.getInt("user_id"))
                    .fullName(rs.getString("full_name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password_hash"))
                    .phone(rs.getString("phone"))
                    .role(rs.getString("role"))
                    .active(rs.getBoolean("is_active"))
                    .dob(rs.getDate("date_of_birth"))
                    .bio(rs.getString("bio"))
                    .gender(rs.getString("gender"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .build(), userId);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE Users SET is_active = ? WHERE user_id = ?";
        jdbc.update(sql, isActive, userId);
    }

    public List<User> findUsersByRolePaginated(String role, int offset, int size) {
        String sql = """
        SELECT * FROM Users
        WHERE role = ?
        ORDER BY full_name
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;

        return jdbc.query(sql, new Object[]{role, offset, size}, (rs, rowNum) -> User.builder()
                .id(rs.getInt("user_id"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .dob(rs.getDate("date_of_birth"))
                .bio(rs.getString("bio"))
                .gender(rs.getString("gender"))
                .avatarUrl(rs.getString("avatar_url"))
                .build());
    }

    public List<User> findUsersByRoleAndSearchPaginated(String role, String keyword, int offset, int size) {
        String sql = """
    SELECT * FROM Users
    WHERE role = ? AND full_name LIKE ?
    ORDER BY full_name
    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;

        String searchKeyword = "%" + keyword + "%";
        return jdbc.query(sql, new Object[]{role, searchKeyword, offset, size}, (rs, rowNum) -> User.builder()
                .id(rs.getInt("user_id"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .dob(rs.getDate("date_of_birth"))
                .bio(rs.getString("bio"))
                .gender(rs.getString("gender"))
                .avatarUrl(rs.getString("avatar_url"))
                .build());
    }


    public int countUsersByRoleAndSearch(String role, String keyword) {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = ? AND full_name LIKE ?";
        String wildcard = "%" + keyword + "%";
        return jdbc.queryForObject(sql, Integer.class, role, wildcard);
    }

    public Page<User> findByRoleAndSearch(String role, String search, Pageable pageable) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Users WHERE 1=1 AND role IN ('CUSTOMER', 'HOTEL_OWNER')");
        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(role)) {
            sqlBuilder.append(" AND role = ?");
            params.add(role);
        }

        if (StringUtils.hasText(search)) {
            sqlBuilder.append(" AND (full_name LIKE ? OR email LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        String countSql = "SELECT count(*) FROM (" + sqlBuilder.toString() + ") as count_table";
        int total = jdbc.queryForObject(countSql, Integer.class, params.toArray());

        sqlBuilder.append(" ORDER BY user_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(pageable.getOffset());
        params.add(pageable.getPageSize());

        List<User> users = jdbc.query(sqlBuilder.toString(), (rs, rowNum) -> User.builder()
                .id(rs.getInt("user_id"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .dob(rs.getDate("date_of_birth"))
                .bio(rs.getString("bio"))
                .gender(rs.getString("gender"))
                .avatarUrl(rs.getString("avatar_url"))
                .build(), params.toArray());

        return new PageImpl<>(users, pageable, total);
    }

    public Page<User> findByRoleAndSearchAndStatus(String role, String search, Boolean isActive, Pageable pageable) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(role)) {
            sqlBuilder.append(" AND role = ?");
            params.add(role);
        }

        if (StringUtils.hasText(search)) {
            sqlBuilder.append(" AND (full_name LIKE ? OR email LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        if (isActive != null) {
            sqlBuilder.append(" AND is_active = ?");
            params.add(isActive);
        }

        String countSql = "SELECT count(*) FROM (" + sqlBuilder.toString() + ") as count_table";
        int total = jdbc.queryForObject(countSql, Integer.class, params.toArray());

        sqlBuilder.append(" ORDER BY user_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(pageable.getOffset());
        params.add(pageable.getPageSize());

        List<User> users = jdbc.query(sqlBuilder.toString(), (rs, rowNum) -> User.builder()
                .id(rs.getInt("user_id"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .dob(rs.getDate("date_of_birth"))
                .bio(rs.getString("bio"))
                .gender(rs.getString("gender"))
                .avatarUrl(rs.getString("avatar_url"))
                .build(), params.toArray());

        return new PageImpl<>(users, pageable, total);
    }

    public long countByIsActive(boolean isActive) {
        String sql = "SELECT COUNT(*) FROM Users WHERE is_active = ?";
        return jdbc.queryForObject(sql, Long.class, isActive);
    }

    public void flagUserById(int userId, String reason) {
        if (reason == null) {
            String sql = "UPDATE Users SET is_flagged = 0, flag_reason = NULL WHERE user_id = ?";
            jdbc.update(sql, userId);
        } else {
            String sql = "UPDATE Users SET is_flagged = 1, flag_reason = ? WHERE user_id = ?";
            jdbc.update(sql, reason, userId);
        }
    }

    public List<User> findAgentsBySearchSorted(String search, String sort, int offset, int size) {
        String baseSql = """
        SELECT u.*, 
               COUNT(DISTINCT h.hotel_id) AS hotel_count, 
               ISNULL(SUM(b.total_price), 0) AS revenue
        FROM Users u
        LEFT JOIN Hotels h ON u.user_id = h.host_id
        LEFT JOIN Bookings b ON h.hotel_id = b.hotel_id AND MONTH(b.check_in) = MONTH(GETDATE()) AND YEAR(b.check_in) = YEAR(GETDATE())
        WHERE u.role = 'HOTEL_OWNER' AND u.full_name LIKE ?
        GROUP BY\s
          u.user_id, u.full_name, u.email, u.password_hash, u.phone,\s
          u.role, u.is_active, u.date_of_birth, u.bio, u.gender,\s
          u.avatar_url, u.is_flagged, u.flag_reason
    """;

        // Add ORDER BY
        String orderByClause;
        switch (sort) {
            case "hotel_desc" -> orderByClause = "ORDER BY hotel_count DESC";
            case "revenue_desc" -> orderByClause = "ORDER BY revenue DESC";
            default -> orderByClause = "ORDER BY u.full_name ASC";
        }

        String pagination = " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        String finalSql = baseSql + " " + orderByClause + pagination;

        String wildcard = "%" + search + "%";

        return jdbc.query(finalSql, new Object[]{wildcard, offset, size}, (rs, rowNum) -> User.builder()
                .id(rs.getInt("user_id"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .active(rs.getBoolean("is_active"))
                .dob(rs.getDate("date_of_birth"))
                .bio(rs.getString("bio"))
                .gender(rs.getString("gender"))
                .avatarUrl(rs.getString("avatar_url"))
                .build());
    }

}


