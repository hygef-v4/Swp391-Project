package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Review;

import java.util.List;

@Repository
public class ReviewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Review> REVIEW_MAPPER = new BeanPropertyRowMapper<>(Review.class);

    // --- SQL Queries ---

    private static final String SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER = """
                SELECT 
                    r.review_id AS reviewId,
                    r.booking_id AS bookingId,
                    r.reviewer_id AS reviewerId,
                    r.rating,
                    r.comment,
                    r.is_public AS isPublic,
                    r.created_at AS createdAt,
                    u.full_name AS fullName,
                    up.avatar_url AS avatarUrl,
                    up.bio
                FROM Reviews r
                JOIN Users u ON r.reviewer_id = u.user_id
                LEFT JOIN UserProfiles up ON u.user_id = up.user_id
                WHERE r.is_public = 1 AND r.rating >= 4 and  u.role ='customer'
                ORDER BY r.rating DESC, r.created_at DESC
            """;


    private static final String SELECT_TOP_5_PUBLIC_POSITIVE_REVIEWS_WITH_USER = SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER.replace("SELECT", "SELECT TOP 5");

    private static final String SECLECT_RECENT_REVIEWS = """
                SELECT 
                    r.review_id AS reviewId,
                    r.booking_id AS bookingId,
                    r.reviewer_id AS reviewerId,
                    r.rating,
                    r.comment,
                    r.is_public AS isPublic,
                    r.created_at AS createdAt
                FROM Reviews r
                WHERE r.is_public = 1 
                ORDER BY r.created_at DESC
            """;
    // --- Repository Methods ---
    public List<Review> getRecentPublicReviews() {
        return jdbcTemplate.query(SECLECT_RECENT_REVIEWS, REVIEW_MAPPER);
    }

    public List<Review> getTopPublicPositiveReviews() {
        return jdbcTemplate.query(SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER, REVIEW_MAPPER);
    }

    public List<Review> getTop5PublicPositiveReviews() {
        return jdbcTemplate.query(SELECT_TOP_5_PUBLIC_POSITIVE_REVIEWS_WITH_USER, REVIEW_MAPPER);
    }
}
