package org.swp391.hotelbookingsystem.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Hotel;

@Repository
public class HotelRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Hotel> HOTEL_MAPPER = new BeanPropertyRowMapper<>(Hotel.class);

    // SQL Queries
    private static final String COUNT_HOTELS = """
            SELECT COUNT(*) FROM Hotels
            """;

    private static final String SELECT_HOTELS_BY_RATING = """
            SELECT h.hotel_id AS hotelId,
                   h.host_id AS hostId,
                   h.hotel_name AS hotelName,
                   h.address,
                   h.description,
                   h.location_id AS locationId,
                   h.hotel_image_url AS hotelImageUrl,
                   h.rating,
                   h.latitude,
                   h.longitude,
                   h.status,
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     h.status, l.city_name
            ORDER BY h.rating DESC
            """;

    private static final String SELECT_TOP_8_HOTELS = SELECT_HOTELS_BY_RATING.replace("SELECT", "SELECT TOP 8");

    private static final String SELECT_HOTEL = """
            SELECT h.hotel_id AS hotelId,
                   h.host_id AS hostId,
                   h.hotel_name AS hotelName,
                   h.address,
                   h.description,
                   h.location_id AS locationId,
                   h.hotel_image_url AS hotelImageUrl,
                   h.rating,
                   h.latitude,
                   h.longitude,
                   h.status,
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     h.status, l.city_name
            ORDER BY h.hotel_id ASC
            """;


    private static final String SELECT_TOP_4_POPULAR_HOTELS = """
                SELECT TOP 4
                                       h.hotel_id AS hotelId,
                                       h.host_id AS hostId,
                                       h.hotel_name AS hotelName,
                                       h.address,
                                       h.description,
                                       h.location_id AS locationId,
                                       h.hotel_image_url AS hotelImageUrl,
                                       h.rating,
                                       h.latitude,
                                       h.longitude,
                                       COUNT(b.booking_id) AS total_bookings,
                                       MIN(r.price) AS min_price,
                                       h.status,
                                       l.city_name AS cityName
                                FROM Hotels h
                                JOIN Rooms r ON h.hotel_id = r.hotel_id
                                JOIN Locations l ON h.location_id = l.location_id
                				JOIN BookingUnits bu ON r.room_id = bu.room_id
                                JOIN Bookings b ON bu.booking_id = b.booking_id
                                WHERE bu.status = 'approved'
                                GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                                         h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude, h.status, l.city_name
                                ORDER BY COUNT(b.booking_id) DESC
            """;

    public HotelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // Methods
    public List<Hotel> getAllHotels() {
        return jdbcTemplate.query(SELECT_HOTEL, HOTEL_MAPPER);
    }

    public List<Hotel> getTop4PopularHotels() {
        return jdbcTemplate.query(SELECT_TOP_4_POPULAR_HOTELS, HOTEL_MAPPER);
    }

    public int countHotels() {
        return jdbcTemplate.queryForObject(COUNT_HOTELS, Integer.class);
    }

    public List<Hotel> getHotelsSortedByRating() {
        return jdbcTemplate.query(SELECT_HOTELS_BY_RATING, HOTEL_MAPPER);
    }

    public List<Hotel> getTop8HighRatedHotels() {
        return jdbcTemplate.query(SELECT_TOP_8_HOTELS, HOTEL_MAPPER);
    }

    public List<Hotel> getHotelsByLocation(int locationId, Date checkin, Date checkout, int maxGuests, int roomQuantity, String name, int min, int max, boolean star) {
        String query = """
                    WITH BookedRooms AS (
                        SELECT 
                            bu.room_id,
                            SUM(bu.quantity) AS booked_quantity
                        FROM BookingUnits bu
                        JOIN Bookings b ON b.booking_id = bu.booking_id
                        WHERE bu.status IN ('approved', 'check_in')
                            AND b.check_out >= ? AND b.check_in <= ?
                        GROUP BY bu.room_id
                    )
                    SELECT h.hotel_id AS hotelId,
                            h.host_id AS hostId,
                            h.hotel_name AS hotelName,
                            h.address,
                            h.description,
                            h.location_id AS locationId,
                            h.hotel_image_url AS hotelImageUrl,
                            h.rating,
                            h.latitude,
                            h.longitude,
                            h.description,
                            MIN(r.price) AS minPrice,
                            SUM(r.max_guests) AS maxGuests,
                            SUM(r.quantity - ISNULL(br.booked_quantity, 0)) AS roomQuantity,
                            l.city_name AS cityName
                    FROM Hotels h
                    JOIN Locations l ON h.location_id = l.location_id
                    LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
                    LEFT JOIN BookedRooms br ON r.room_id = br.room_id
                    WHERE (h.location_id = ? OR ? = -1) 
                        AND h.hotel_name like ?
                    GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                        h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                        l.city_name
                    HAVING SUM(r.max_guests) >= ?
                        AND SUM(r.quantity - ISNULL(br.booked_quantity, 0)) >= ?
                        AND MIN(r.price) >= ? AND MIN(r.price) <= ?
                """;
        String order = " ORDER BY h.rating " + (star ? "ASC" : "DESC");

        return jdbcTemplate.query(query+order, ps -> {
            ps.setDate(1, checkin != null ? checkin : new Date(System.currentTimeMillis()));
            ps.setDate(2, checkout != null ? checkout : new Date(System.currentTimeMillis()));
            ps.setInt(3, locationId);
            ps.setInt(4, locationId);
            ps.setString(5, "%" + name + "%");
            ps.setInt(6, maxGuests);
            ps.setInt(7, roomQuantity);
            ps.setInt(8, min);
            ps.setInt(9, max);
        }, HOTEL_MAPPER);
    }

    public List<Hotel> searchHotel(String search) {
        String query = """
                SELECT h.hotel_id AS hotelId,
                       h.host_id AS hostId,
                       h.hotel_name AS hotelName,
                       h.address,
                       h.description,
                       h.location_id AS locationId,
                       h.hotel_image_url AS hotelImageUrl,
                       h.rating,
                       h.latitude,
                       h.longitude,
                       h.status,
                       MIN(r.price) AS minPrice,
                       l.city_name AS cityName
                FROM Hotels h
                JOIN Locations l ON h.location_id = l.location_id
                JOIN Rooms r ON h.hotel_id = r.hotel_id
                WHERE h.hotel_name like ?
                GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                         h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                         h.status, l.city_name
                """;
        return jdbcTemplate.query(query, ps -> {
            ps.setString(1, "%" + search + "%");
        }, HOTEL_MAPPER);
    }

    public Hotel getHotelById(int id) {
        String query = """
                SELECT h.hotel_id AS hotelId,
                       h.host_id AS hostId,
                       h.hotel_name AS hotelName,
                       h.address,
                       h.description,
                       h.location_id AS locationId,
                       h.hotel_image_url AS hotelImageUrl,
                       h.rating,
                       h.latitude,
                       h.longitude,
                       h.policy,
                       h.status,
                       MIN(r.price) AS minPrice,
                       l.city_name AS cityName
                FROM Hotels h
                JOIN Locations l ON h.location_id = l.location_id
                JOIN Rooms r ON h.hotel_id = r.hotel_id
                WHERE h.hotel_id like ?
                GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                         h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude, h.policy,
                         l.city_name, h.status
                """;
        return jdbcTemplate.queryForObject(query, HOTEL_MAPPER, id);
    }

    public Hotel insertHotel(Hotel hotel) {
        String sql = """
                    INSERT INTO Hotels 
                    (host_id, hotel_name, address, location_id, latitude, longitude, hotel_image_url, rating, description, policy)
                    OUTPUT INSERTED.hotel_id
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Integer hotelId = jdbcTemplate.queryForObject(sql, Integer.class,
                hotel.getHostId(),
                hotel.getHotelName(),
                hotel.getAddress(),
                hotel.getLocationId(),
                hotel.getLatitude(),
                hotel.getLongitude(),
                hotel.getHotelImageUrl(),
                hotel.getRating(),  // can be null
                hotel.getDescription(),
                hotel.getPolicy()
        );

        if (hotelId == null) {
            throw new IllegalStateException("Failed to insert hotel, no ID returned.");
        }

        hotel.setHotelId(hotelId);
        return hotel;
    }

    public void updateHotel(Hotel hotel) {
        String sql = """
                    UPDATE Hotels 
                    SET hotel_name = ?, 
                        address = ?, 
                        location_id = ?, 
                        latitude = ?, 
                        longitude = ?, 
                        hotel_image_url = ?, 
                        description = ?, 
                        policy = ?
                    WHERE hotel_id = ?
                """;

        jdbcTemplate.update(sql,
                hotel.getHotelName(),
                hotel.getAddress(),
                hotel.getLocationId(),
                hotel.getLatitude(),
                hotel.getLongitude(),
                hotel.getHotelImageUrl(),
                hotel.getDescription(),
                hotel.getPolicy(),
                hotel.getHotelId()
        );
    }

    public List<Hotel> findByHostId(int hostId) {
        String sql = """
                    SELECT h.hotel_id AS hotelId,
                           h.host_id AS hostId,
                           h.hotel_name AS hotelName,
                           h.address,
                           h.description,
                           h.location_id AS locationId,
                           h.hotel_image_url AS hotelImageUrl,
                           h.rating,
                           h.latitude,
                           h.longitude,
                           h.policy,
                           MIN(r.price) AS minPrice,
                           l.city_name AS cityName
                    FROM Hotels h
                    JOIN Locations l ON h.location_id = l.location_id
                    LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
                    WHERE h.host_id = ?
                    GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                             h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                             h.policy, l.city_name
                    ORDER BY h.hotel_id DESC
                """;

        return jdbcTemplate.query(sql, new Object[]{hostId}, HOTEL_MAPPER);
    }

    public Double findAverageRatingByHostId(int hostId) {
        String sql = """
                SELECT CASE WHEN COUNT(*) = 0 THEN NULL
                            ELSE SUM(h.rating) / COUNT(*) END AS averageRating
                FROM Hotels h
                WHERE h.host_id = ?
            """;
        return jdbcTemplate.queryForObject(sql, new Object[]{hostId}, Double.class);
    }


    public void deleteHotelById(int hotelId) {
        String sql = "DELETE FROM Hotels WHERE hotel_id = ?";
        jdbcTemplate.update(sql, hotelId);
    }

    public Integer isFavoriteHotel(int userId, int hotelId) {
        String sql = """
                SELECT COUNT(*) FROM Favorites
                WHERE user_id = ? AND hotel_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, hotelId);
    }

    public void insertHotelDeletionToken(int userId, String token, LocalDateTime expiry, String tokenType) {
        String sql = "INSERT INTO Tokens (user_id, token, expiry_date, token_type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, token, Timestamp.valueOf(expiry), tokenType);
    }

    public String findValidTokenType(String token, int userId) {
        String sql = "SELECT token_type FROM Tokens WHERE token = ? AND user_id = ? AND expiry_date > GETDATE()";
        return jdbcTemplate.queryForObject(sql, String.class, token, userId);
    }


    public void cancelHotelDeleteToken(int userId, int hotelId) {
        String sql = "DELETE FROM Tokens WHERE user_id = ? AND token LIKE ?";
        jdbcTemplate.update(sql, userId, "%:" + hotelId);
    }

    public int countHotelByHostId(int hostId) {
        String sql = "SELECT COUNT(*) FROM Hotels WHERE host_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, hostId);
    }

    public int updateHotelStatus(int hotelId, String status) {
        String sql = "UPDATE Hotels SET status = ? WHERE hotel_id = ?";
        return jdbcTemplate.update(sql, status, hotelId);
    }

    public int countBookingsByHotelId(int hotelId) {
        String sql = """
        SELECT COUNT(*) FROM BookingUnits bu
        JOIN Rooms r ON bu.room_id = r.room_id
        WHERE r.hotel_id = ?
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, hotelId);
    }

    public List<Hotel> searchHotelsPaginated(String search, int offset, int limit) {
        String query = """
        SELECT h.hotel_id AS hotelId,
               h.host_id AS hostId,
               h.hotel_name AS hotelName,
               h.address,
               h.description,
               h.location_id AS locationId,
               h.hotel_image_url AS hotelImageUrl,
               h.rating,
               h.latitude,
               h.longitude,
               MIN(r.price) AS minPrice,
               l.city_name AS cityName,
               u.full_name AS hostName
        FROM Hotels h
        JOIN Locations l ON h.location_id = l.location_id
        JOIN Users u ON h.host_id = u.user_id
        LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
        WHERE h.hotel_name LIKE ?
        GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                 h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                 l.city_name, u.full_name
        ORDER BY h.hotel_id
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;

        return jdbcTemplate.query(query, ps -> {
            ps.setString(1, "%" + (search == null ? "" : search.trim()) + "%");
            ps.setInt(2, offset);
            ps.setInt(3, limit);
        }, HOTEL_MAPPER);
    }

    public int countHotelsBySearch(String search) {
        String sql = "SELECT COUNT(*) FROM Hotels WHERE hotel_name LIKE ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, "%" + (search == null ? "" : search.trim()) + "%");
    }
}
