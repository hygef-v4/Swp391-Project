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

    public static class RatingStats {
        private final double average;
        private final int count;

        public RatingStats(double average, int count) {
            this.average = average;
            this.count = count;
        }

        public double getAverage() {
            return average;
        }

        public int getCount() {
            return count;
        }
    }

    public Optional<RatingStats> getAverageRatingByHostId(int hostId) {
        String sql = """
            SELECT AVG(CAST(r.rating AS FLOAT)) as average_rating, COUNT(r.review_id) as review_count
            FROM Reviews r
            JOIN Hotels h ON r.hotel_id = h.hotel_id
            WHERE h.host_id = ?
        """;

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            double average = rs.getDouble("average_rating");
            int count = rs.getInt("review_count");
            if (rs.wasNull()) {
                return new RatingStats(0.0, 0);
            }
            return new RatingStats(average, count);
        }, hostId));
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
