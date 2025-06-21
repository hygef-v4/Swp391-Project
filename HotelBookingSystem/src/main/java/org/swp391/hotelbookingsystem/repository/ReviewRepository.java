package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Review;

import java.util.List;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Review> REVIEW_MAPPER = new BeanPropertyRowMapper<>(Review.class);

    private static final String SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER = """
                SELECT 
                    r.review_id AS reviewId,
                    r.hotel_id AS hotelId,
                    r.reviewer_id AS reviewerId,
                    r.rating,
                    r.comment,
                    r.is_public AS isPublic,
                    r.created_at AS createdAt,
                    u.full_name AS fullName,
                    u.avatar_url AS avatarUrl,
                    u.bio,
                    u.date_of_birth AS dob
                FROM Reviews r
                JOIN Users u ON r.reviewer_id = u.user_id
                WHERE r.is_public = 1 AND r.rating >= 4 AND u.role = 'CUSTOMER'
                ORDER BY r.rating DESC, r.created_at DESC
            """;

    private static final String SELECT_TOP_5_PUBLIC_POSITIVE_REVIEWS_WITH_USER =
            SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER.replace("SELECT", "SELECT TOP 5");

    private static final String SELECT_RECENT_REVIEWS = """
        SELECT TOP 4
            r.review_id AS reviewId,
            r.hotel_id AS hotelId,
            r.reviewer_id AS reviewerId,
            r.rating,
            r.comment,
            r.is_public AS isPublic,
            r.created_at AS createdAt,
            u.full_name AS fullName,
            u.avatar_url AS avatarUrl,
            u.bio,
            u.date_of_birth AS dob
        FROM Reviews r
        JOIN Users u ON r.reviewer_id = u.user_id
        WHERE  r.is_public = 1 AND u.role = 'CUSTOMER'
        ORDER BY r.created_at DESC
    """;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- Repository Methods ---

    public List<Review> getRecentPublicReviews() {
        return jdbcTemplate.query(SELECT_RECENT_REVIEWS, REVIEW_MAPPER);
    }

    public List<Review> getTopPublicPositiveReviews() {
        return jdbcTemplate.query(SELECT_TOP_PUBLIC_POSITIVE_REVIEWS_WITH_USER, REVIEW_MAPPER);
    }

    public List<Review> getTop5PublicPositiveReviews() {
        return jdbcTemplate.query(SELECT_TOP_5_PUBLIC_POSITIVE_REVIEWS_WITH_USER, REVIEW_MAPPER);
    }

    public boolean checkReview(int hotelId, int userId){
        String query = """
            SELECT * FROM Reviews
            WHERE hotel_id = ? AND reviewer_id = ?        
        """;
        return jdbcTemplate.query(query, ps -> {
            ps.setInt(1, hotelId);
            ps.setInt(2, userId);
        }, REVIEW_MAPPER).isEmpty();
    }

    public int addReview(Review review){
        String query = "INSERT INTO Reviews (hotel_id, reviewer_id, rating, comment) OUTPUT inserted.review_id VALUES (?, ?, ?, ?)";
        return jdbcTemplate.queryForObject(query, Integer.class, review.getHotelId(), review.getReviewerId(), review.getRating(), review.getComment());
    }

    public Review getReviewById(int id){
        String query = """
            SELECT
            r.review_id AS reviewId,
            r.hotel_id AS hotelId,
            r.reviewer_id AS reviewerId,
            r.rating,
            r.comment,
            r.is_public AS isPublic,
            r.created_at AS createdAt,
            u.full_name AS fullName,
            u.avatar_url AS avatarUrl,
            u.bio,
            u.date_of_birth AS dob
        FROM Reviews r
        JOIN Users u ON r.reviewer_id = u.user_id
        WHERE r.is_public = 1 AND r.review_id = ?
        """;
        return jdbcTemplate.queryForObject(query, REVIEW_MAPPER, id);
    }

    public List<Review> getHotelReview(int hotelId){
        String query = """
            SELECT
            r.review_id AS reviewId,
            r.hotel_id AS hotelId,
            r.reviewer_id AS reviewerId,
            r.rating,
            r.comment,
            r.is_public AS isPublic,
            r.created_at AS createdAt,
            u.full_name AS fullName,
            u.avatar_url AS avatarUrl,
            u.bio,
            u.date_of_birth AS dob
        FROM Reviews r
        JOIN Users u ON r.reviewer_id = u.user_id
        WHERE r.is_public = 1 AND r.hotel_id = ?
        """;
        return jdbcTemplate.query(query, REVIEW_MAPPER, hotelId);
    }
}
