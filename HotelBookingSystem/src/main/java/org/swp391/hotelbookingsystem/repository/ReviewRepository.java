package org.swp391.hotelbookingsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Review;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Double getAverageHotelRatingByHostId(int hostId) {
        String sql = """
        SELECT AVG(CAST(rating AS FLOAT)) 
        FROM Hotels 
        WHERE host_id = ?
    """;
        return jdbcTemplate.queryForObject(sql, Double.class, hostId);
    }


    public List<Review> getRecentPublicReviews() {
        String sql = """
            SELECT TOP 5
                r.review_id, r.hotel_id, r.reviewer_id, r.rating, r.comment, r.is_public, r.created_at,
                u.full_name, u.avatar_url
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            WHERE r.is_public = 1
            ORDER BY r.created_at DESC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Review.builder()
                        .reviewId(rs.getInt("review_id"))
                        .hotelId(rs.getInt("hotel_id"))
                        .reviewerId(rs.getInt("reviewer_id"))
                        .rating(rs.getInt("rating"))
                        .comment(rs.getString("comment"))
                        .isPublic(rs.getBoolean("is_public"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .fullName(rs.getString("full_name"))
                        .avatarUrl(rs.getString("avatar_url"))
                        .build()
        );
    }

    public List<Review> getTop5PublicPositiveReviews() {
        String sql = """
            SELECT TOP 5
                r.review_id, r.hotel_id, r.reviewer_id, r.rating, r.comment, r.is_public, r.created_at,
                u.full_name, u.avatar_url
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            WHERE r.is_public = 1 AND r.rating >= 4
            ORDER BY r.rating DESC, r.created_at DESC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Review.builder()
                        .reviewId(rs.getInt("review_id"))
                        .hotelId(rs.getInt("hotel_id"))
                        .reviewerId(rs.getInt("reviewer_id"))
                        .rating(rs.getInt("rating"))
                        .comment(rs.getString("comment"))
                        .isPublic(rs.getBoolean("is_public"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .fullName(rs.getString("full_name"))
                        .avatarUrl(rs.getString("avatar_url"))
                        .build()
        );
    }
}
