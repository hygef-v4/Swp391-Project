package org.swp391.hotelbookingsystem.repository;

import java.util.*;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Reply;
import org.swp391.hotelbookingsystem.model.Review;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Review> REVIEW_MAPPER = new BeanPropertyRowMapper<>(Review.class);
    private static final BeanPropertyRowMapper<Reply> REPLY_MAPPER = new BeanPropertyRowMapper<>(Reply.class);

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
                WHERE r.is_public = 1 AND r.rating >= 4 AND u.role = 'CUSTOMER' AND r.comment IS NOT NULL
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
        WHERE r.is_public = 1 AND u.role = 'CUSTOMER' AND r.comment IS NOT NULL
        ORDER BY r.created_at DESC
    """;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Double getAverageHotelRatingByHostId(int hostId) {
        String sql = """
        SELECT AVG(CAST(r.rating AS FLOAT))
        FROM Reviews r
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        WHERE h.host_id = ?
        AND r.is_public = 1
        AND r.comment IS NOT NULL
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
            WHERE r.is_public = 1 AND r.comment IS NOT NULL
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
            WHERE r.is_public = 1 AND r.rating >= 4 AND r.comment IS NOT NULL
            ORDER BY r.rating DESC
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

    public boolean checkReview(int hotelId, int userId){
        String query = """
            SELECT * FROM Reviews
            WHERE hotel_id = ? AND reviewer_id = ? AND is_public = 1 AND comment IS NOT NULL  
        """;
        return !jdbcTemplate.query(query, ps -> {
            ps.setInt(1, hotelId);
            ps.setInt(2, userId);
        }, REVIEW_MAPPER).isEmpty();
    }

    public Review getReview(int hotelId, int userId){
        String query = """
            SELECT
                r.review_id AS reviewId,
                r.hotel_id AS hotelId,
                r.reviewer_id AS reviewerId,
                r.rating,
                r.comment,
                r.is_public AS isPublic,
                r.created_at AS createdAt,
                u.user_id AS userId,
                u.full_name AS fullName,
                u.avatar_url AS avatarUrl,
                u.bio,
                u.date_of_birth AS dob
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            WHERE r.is_public = 1 AND r.hotel_id = ? AND r.reviewer_id = ? AND r.comment IS NOT NULL
        """;
        return jdbcTemplate.queryForObject(query, REVIEW_MAPPER, hotelId, userId);
    }

    public int addReview(Review review){
        String query = """
            DECLARE @output TABLE (reviewId INT);
            INSERT INTO Reviews (hotel_id, reviewer_id, rating, comment) OUTPUT inserted.review_id INTO @output VALUES (?, ?, ?, ?)  
            SELECT * FROM @output;
        """;
        return jdbcTemplate.queryForObject(query, Integer.class, review.getHotelId(), review.getReviewerId(), review.getRating(), review.getComment());
    }

    public int editReview(Review review){
        String query = """
            DECLARE @output TABLE (reviewId INT);
            UPDATE Reviews SET rating = ?, comment = ? OUTPUT inserted.review_id INTO @output WHERE hotel_id = ? AND reviewer_id = ? AND is_public = 1
            SELECT * FROM @output;
        """;
        return jdbcTemplate.queryForObject(query, Integer.class, review.getRating(), review.getComment(), review.getHotelId(), review.getReviewerId());
    }

    public int deleteReview(int hotelId, int userId){
        String query = """
            DECLARE @output TABLE (reviewId INT);
            UPDATE Reviews SET is_public = 0 OUTPUT deleted.review_id INTO @output WHERE hotel_id = ? AND reviewer_id = ? AND is_public = 1
            SELECT * FROM @output;
        """;
        return jdbcTemplate.queryForObject(query, Integer.class, hotelId, userId);
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
                u.user_id AS userId,
                u.full_name AS fullName,
                u.avatar_url AS avatarUrl,
                u.bio,
                u.date_of_birth AS dob
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            WHERE r.is_public = 1 AND r.review_id = ? AND r.comment IS NOT NULL
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
                u.user_id AS userId,
                u.full_name AS fullName,
                u.avatar_url AS avatarUrl,
                u.bio,
                u.date_of_birth AS dob
            FROM Reviews r
            JOIN Users u ON r.reviewer_id = u.user_id
            WHERE r.is_public = 1 AND r.hotel_id = ? AND r.comment IS NOT NULL
            ORDER BY r.created_at DESC
        """;
        return jdbcTemplate.query(query, REVIEW_MAPPER, hotelId);
    }

    public int addReply(Reply reply){
        String query = "INSERT INTO Replies (review_id, replier_id, comment) OUTPUT inserted.reply_id VALUES (?, ?, ?)";
        return jdbcTemplate.queryForObject(query, Integer.class, reply.getReviewId(), reply.getReplierId(), reply.getComment());
    }

    public int editReply(int replyId, String comment){
        String query = "UPDATE Replies SET comment = ? OUTPUT inserted.reply_id WHERE reply_id = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, comment, replyId);
    }

    public int deleteReply(int replyId){
        String query = "UPDATE Replies SET is_public = 0 OUTPUT deleted.reply_id WHERE reply_id = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, replyId);
    }

    public Reply getReplyById(int id){
        String query = """
            SELECT
                r.reply_id AS replyId,
                r.review_id AS reviewId,
                r.replier_id AS replierId,
                r.comment,
                r.is_public AS isPublic,
                r.created_at AS createdAt,
                u.user_id AS userId,
                u.full_name AS fullName,
                u.avatar_url AS avatarUrl,
                u.bio,
                u.date_of_birth AS dob
            FROM Replies r
            JOIN Users u ON r.replier_id = u.user_id
            WHERE r.is_public = 1 AND r.reply_id = ? AND r.comment IS NOT NULL
        """;
        return jdbcTemplate.queryForObject(query, REPLY_MAPPER, id);
    }

    public List<Reply> getReviewReply(int reviewId){
        String query = """
            SELECT
                r.reply_id AS replyId,
                r.review_id AS reviewId,
                r.replier_id AS replierId,
                r.comment,
                r.is_public AS isPublic,
                r.created_at AS createdAt,
                u.user_id AS userId,
                u.full_name AS fullName,
                u.avatar_url AS avatarUrl,
                u.bio,
                u.date_of_birth AS dob
            FROM Replies r
            JOIN Users u ON r.replier_id = u.user_id
            WHERE r.is_public = 1 AND r.review_id = ? AND r.comment IS NOT NULL
            ORDER BY r.created_at ASC
        """;
        return jdbcTemplate.query(query, REPLY_MAPPER, reviewId);
    }

    public double getAverageRatingThisYear() {
        String sql = """
        SELECT AVG(CAST(rating AS FLOAT))
        FROM Reviews
        WHERE YEAR(created_at) = YEAR(GETDATE())
    """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class);
        return result != null ? result : 0.0;
    }

    public double getOverallAverageRating() {
        String sql = """
        SELECT AVG(CAST(rating AS FLOAT))
        FROM Reviews
        WHERE is_public = 1 AND comment IS NOT NULL
    """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class);
        return result != null ? result : 0.0;
    }

    public int countAllReviews() {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE is_public = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int countReviewsThisYear() {
        String sql = """
        SELECT COUNT(*) 
        FROM Reviews 
        WHERE is_public = 1 
          AND YEAR(created_at) = YEAR(GETDATE())
          AND comment IS NOT NULL
    """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    public int countReviewsLastYear() {
        String sql = """
        SELECT COUNT(*) 
        FROM Reviews 
        WHERE is_public = 1 
          AND YEAR(created_at) = YEAR(GETDATE()) - 1
          AND comment IS NOT NULL
    """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }



    public List<Integer> getRatingDistribution() {
        String sql = """
        SELECT rating, COUNT(*) AS count 
        FROM Reviews 
        WHERE is_public = 1 AND comment IS NOT NULL
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
            WHERE r.is_public = 1 AND r.comment IS NOT NULL
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
            case "published" -> "r.is_public = 1 AND r.comment IS NOT NULL";
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

    public int countTotalReviewsByHostId(int hostId) {
        String sql = """
        SELECT COUNT(*) FROM Reviews r
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        WHERE h.host_id = ?
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, hostId);
    }

    public int countUnaddressedReviewsByHostId(int hostId) {
        String sql = """
        SELECT COUNT(*) FROM Reviews r
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        LEFT JOIN Replies rp ON r.review_id = rp.review_id
        WHERE h.host_id = ? AND rp.review_id IS NULL
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, hostId);
    }

    public int countRecentReviewsByHostId(int hostId) {
        String sql = """
        SELECT COUNT(*) FROM Reviews r
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        WHERE h.host_id = ? AND r.created_at >= DATEADD(day, -30, GETDATE())
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, hostId);
    }

    public List<Review> getReviewsByHostId(int hostId) {
        String sql = """
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
            h.hotel_name AS hotelName
        FROM Reviews r
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        JOIN Users u ON r.reviewer_id = u.user_id
        WHERE h.host_id = ? AND r.is_public = 1 AND r.comment IS NOT NULL
        ORDER BY r.created_at DESC
    """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Review.class), hostId);
    }

    public boolean hasOtherPublicReview(int reviewerId, int hotelId, int excludedReviewId) {
        String sql = """
        SELECT COUNT(*) FROM Reviews
        WHERE is_public = 1 AND comment IS NOT NULL
        AND reviewer_id = ? AND hotel_id = ?
        AND review_id <> ?
    """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewerId, hotelId, excludedReviewId);
        return count != null && count > 0;
    }

    public Review getReviewByIdIncludingDeleted(int id) {
        String sql = """
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
            h.hotel_name AS hotelName
        FROM Reviews r
        JOIN Users u ON r.reviewer_id = u.user_id
        JOIN Hotels h ON r.hotel_id = h.hotel_id
        WHERE r.review_id = ?
    """;

        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Review.class), id);
    }

}
