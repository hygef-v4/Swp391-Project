package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.dto.RecentRoomBookingDTO;
import org.swp391.hotelbookingsystem.dto.ReviewDTO;
import org.swp391.hotelbookingsystem.dto.UpcomingGuestDTO;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Room;

import java.util.*;

@Repository
public class AdminDashboardRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Hotel> HOTEL_MAPPER = new BeanPropertyRowMapper<>(Hotel.class);

    // SQL Queries
    private static final String COUNT_HOTELS = """
            SELECT COUNT(*) FROM Hotels
            """;

    private static final String COUNT_ROOMS = "SELECT SUM(quantity) FROM Rooms";

    private static final String COUNT_BOOKED_ROOMS = """
            SELECT COUNT(*) FROM Rooms WHERE status = 'booked'
            """;

    private static final String COUNT_AVAILABLE_ROOMS = """
            SELECT COUNT(*) FROM Rooms WHERE status = 'active'
            """;

    private static final String RECENT_ROOM_BOOKINGS = """
            SELECT TOP 5 r.room_id, r.title, b.check_in, b.check_out, r.status,
                   (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = r.room_id) AS image_url
            FROM Bookings b
            JOIN Rooms r ON b.room_id = r.room_id
            ORDER BY b.created_at DESC
            """;

    private static final String TOP_POPULAR_HOTELS = """
            SELECT TOP 5 h.hotel_id, h.hotel_name, h.hotel_image_url, h.address, COUNT(b.booking_id) AS total_bookings,
                   MIN(r.price) AS min_price
            FROM Hotels h
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            JOIN Bookings b ON r.room_id = b.room_id
            GROUP BY h.hotel_id, h.hotel_name, h.hotel_image_url, h.address
            ORDER BY COUNT(b.booking_id) DESC
            """;

    private static final String UPCOMING_GUESTS = """
            SELECT TOP 5 u.user_id, u.full_name, r.title AS room_name, b.check_in, b.check_out,
                   p.avatar_url
            FROM Bookings b
            JOIN Users u ON b.customer_id = u.user_id
            JOIN Rooms r ON b.room_id = r.room_id
            LEFT JOIN UserProfiles p ON u.user_id = p.user_id
            WHERE b.check_in >= GETDATE()
            ORDER BY b.check_in ASC
            """;

    private static final String RECENT_REVIEWS = """
            SELECT TOP 5 r.review_id, rm.title AS room_title, r.rating,
                   (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = rm.room_id) AS image_url
            FROM Reviews r
            JOIN Bookings b ON r.booking_id = b.booking_id
            JOIN Rooms rm ON b.room_id = rm.room_id
            ORDER BY r.created_at DESC
            """;

    public LinkedHashMap<String, Integer> getCheckInCountsByDate() {
        String sql = """
        SELECT CONVERT(VARCHAR, check_in, 23) AS date_str, COUNT(*) AS count
        FROM bookings
        WHERE status = 'approved'
        GROUP BY CONVERT(VARCHAR, check_in, 23)
        ORDER BY date_str
    """;

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getString("date_str"), rs.getInt("count"));
        });
        return result;
    }

    public LinkedHashMap<String, Integer> getCheckOutCountsByDate() {
        String sql = """
        SELECT CONVERT(VARCHAR, check_out, 23) AS date_str, COUNT(*) AS count
        FROM bookings
        WHERE status = 'approved'
        GROUP BY CONVERT(VARCHAR, check_out, 23)
        ORDER BY date_str
    """;

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getString("date_str"), rs.getInt("count"));
        });
        return result;
    }

    // Methods
    public long countHotels() {
        return jdbcTemplate.queryForObject(COUNT_HOTELS, Long.class);
    }

    public long countRooms() {
        return jdbcTemplate.queryForObject(COUNT_ROOMS, Long.class);
    }

    public long countBookedRooms() {
        return jdbcTemplate.queryForObject(COUNT_BOOKED_ROOMS, Long.class);
    }

    public long countAvailableRooms() {
        return jdbcTemplate.queryForObject(COUNT_AVAILABLE_ROOMS, Long.class);
    }


    public int getTotalBookedRooms() {
        Integer count = jdbcTemplate.queryForObject(COUNT_BOOKED_ROOMS, Integer.class);
        return count != null ? count : 0;
    }

    public List<Hotel> findTopPopularHotels() {
        return jdbcTemplate.query(TOP_POPULAR_HOTELS, (rs, rowNum) -> {
            Hotel h = new Hotel();
            h.setHotelId(rs.getInt("hotel_id"));
            h.setHotelName(rs.getString("hotel_name"));
            h.setHotelImageUrl(rs.getString("hotel_image_url"));
            h.setAddress(rs.getString("address"));
            h.setMinPrice(rs.getBigDecimal("min_price"));
            return h;
        });
    }

    public List<RecentRoomBookingDTO> findRecentRoomBookings() {
        return jdbcTemplate.query(RECENT_ROOM_BOOKINGS, (rs, rowNum) -> {
            RecentRoomBookingDTO dto = new RecentRoomBookingDTO();
            dto.id = rs.getLong("room_id");
            dto.name = rs.getString("title");
            dto.dateRange = rs.getDate("check_in") + " to " + rs.getDate("check_out");
            dto.status = rs.getString("status");
            dto.imageUrl = rs.getString("image_url");
            return dto;
        });
    }

    public List<UpcomingGuestDTO> findUpcomingGuests() {
        return jdbcTemplate.query(UPCOMING_GUESTS, (rs, rowNum) -> {
            UpcomingGuestDTO dto = new UpcomingGuestDTO();
            dto.id = rs.getLong("user_id");
            dto.name = rs.getString("full_name");
            dto.room = rs.getString("room_name");
            dto.dateRange = rs.getDate("check_in") + " - " + rs.getDate("check_out");
            dto.avatarUrl = rs.getString("avatar_url");
            return dto;
        });
    }

    public List<ReviewDTO> findRecentReviews() {
        return jdbcTemplate.query(RECENT_REVIEWS, (rs, rowNum) -> {
            ReviewDTO dto = new ReviewDTO();
            dto.id = rs.getLong("review_id");
            dto.roomName = rs.getString("room_title");
            dto.starCount = rs.getInt("rating");
            dto.imageUrl = rs.getString("image_url");

            StringBuilder stars = new StringBuilder();
            for (int i = 1; i <= 5; i++) {
                stars.append(i <= dto.starCount ? "<i class='fas fa-star text-warning'></i>"
                        : "<i class='far fa-star text-warning'></i>");
            }
            dto.starsHtml = stars.toString();
            return dto;
        });
    }
}
