package org.swp391.hotelbookingsystem.repository;

import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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

    public double getAverageRating() {
        String sql = "SELECT AVG(CAST(rating AS FLOAT)) FROM Reviews";
        Double result = jdbcTemplate.queryForObject(sql, Double.class);
        return result != null ? result : 0.0;
    }

    public List<Review> getReviewsByStatus(String status, int offset, int limit) {
        String condition = switch (status.toLowerCase()) {
            case "published" -> "r.is_public = 1";
            case "deleted" -> "r.is_public = 0";
            default -> "1=1";
        };

        String sql = String.format("""
    SELECT r.review_id, r.hotel_id, r.reviewer_id, r.rating, r.comment, r.is_public, r.created_at,
           u.full_name, u.avatar_url,
           h.hotel_name
    FROM Reviews r
    JOIN Users u ON r.reviewer_id = u.user_id
    JOIN Hotels h ON r.hotel_id = h.hotel_id
    WHERE %s
    ORDER BY r.created_at DESC
    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
""", condition);

        return jdbcTemplate.query(sql, (rs, rowNum) -> Review.builder()
                .reviewId(rs.getInt("review_id"))
                .hotelId(rs.getInt("hotel_id"))
                .reviewerId(rs.getInt("reviewer_id"))
                .rating(rs.getInt("rating"))
                .comment(rs.getString("comment"))
                .isPublic(rs.getBoolean("is_public"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .fullName(rs.getString("full_name"))
                .avatarUrl(rs.getString("avatar_url"))
                .hotelName(rs.getString("hotel_name"))
                .build(), offset, limit);

    }

    public int countAllReviews() {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE is_public = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Integer> getRatingDistribution() {
        String sql = """
        SELECT rating, COUNT(*) AS count 
        FROM Reviews 
        WHERE is_public = 1 
        GROUP BY rating
    """;

        ResultSetExtractor<Map<Integer, Integer>> extractor = rs -> {
            Map<Integer, Integer> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getInt("rating"), rs.getInt("count"));
            }
            return map;
        };

        Map<Integer, Integer> ratingMap = jdbcTemplate.query(sql, extractor);

        List<Integer> distribution = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            distribution.add(ratingMap.getOrDefault(i, 0));
        }
        return distribution;
    }

    public int countByPublic(boolean isPublic) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE is_public = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, isPublic);
    }

    public void setPublicStatus(int reviewId, boolean isPublic) {
        String sql = "UPDATE Reviews SET is_public = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, isPublic, reviewId);
    }

    public List<Review> getAllPublicReviews() {
        String sql = """
            SELECT r.review_id, r.hotel_id, r.reviewer_id, r.rating, r.comment, r.is_public, r.created_at,
                   u.full_name, u.avatar_url,
                   h.hotel_name
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            JOIN Hotels h ON r.hotel_id = h.hotel_id
            WHERE r.is_public = 1
            ORDER BY r.created_at DESC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> Review.builder()
                .reviewId(rs.getInt("review_id"))
                .hotelId(rs.getInt("hotel_id"))
                .reviewerId(rs.getInt("reviewer_id"))
                .rating(rs.getInt("rating"))
                .comment(rs.getString("comment"))
                .isPublic(rs.getBoolean("is_public"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .fullName(rs.getString("full_name"))
                .avatarUrl(rs.getString("avatar_url"))
                .hotelName(rs.getString("hotel_name"))
                .build());
    }
    public List<Review> getReviewsByStatus(String status) {
        String condition = switch (status.toLowerCase()) {
            case "published" -> "r.is_public = 1";
            case "deleted" -> "r.is_public = 0";
            default -> "1=1";
        };

        String sql = String.format("""
        SELECT r.review_id, r.hotel_id, r.reviewer_id, r.rating, r.comment, r.is_public, r.created_at,
               u.full_name, u.avatar_url,
               h.hotel_name
        FROM Reviews r
        JOIN Users u ON r.reviewer_id = u.user_id
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        WHERE %s
        ORDER BY r.created_at DESC
    """, condition);

        return jdbcTemplate.query(sql, (rs, rowNum) -> Review.builder()
                .reviewId(rs.getInt("review_id"))
                .hotelId(rs.getInt("hotel_id"))
                .reviewerId(rs.getInt("reviewer_id"))
                .rating(rs.getInt("rating"))
                .comment(rs.getString("comment"))
                .isPublic(rs.getBoolean("is_public"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .fullName(rs.getString("full_name"))
                .avatarUrl(rs.getString("avatar_url"))
                .hotelName(rs.getString("hotel_name"))
                .build());
    }

}
