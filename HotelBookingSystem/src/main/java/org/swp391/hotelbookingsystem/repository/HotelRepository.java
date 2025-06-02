package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Location;

import java.util.List;

@Repository
public class HotelRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Hotel> HOTEL_MAPPER = new BeanPropertyRowMapper<>(Hotel.class);

    // SQL Queries
    private static final String COUNT_HOTELS = """
            SELECT COUNT(*) FROM Hotels
            """;

    private static final String SELECT_HOTEL_BY_LOCATION = """
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
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
            WHERE h.location_id = ? AND h.hotel_name like ?
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
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
                   h.description,
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
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
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
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
                   MIN(r.price) AS min_price
            FROM Hotels h
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            JOIN Bookings b ON r.room_id = b.room_id
            JOIN Locations l ON h.location_id = l.location_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude
            ORDER BY COUNT(b.booking_id) DESC
            """;


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

    public List<Hotel> getHotelsByLocation(int locationId, String search) {
        return jdbcTemplate.query(SELECT_HOTEL_BY_LOCATION, ps -> {
            ps.setInt(1, locationId);
            ps.setString(2, "%" + search + "%");
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
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            WHERE h.hotel_name like ?
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
            """;            
        return jdbcTemplate.query(query, ps -> {
            ps.setString(1, "%" + search + "%");
        }, HOTEL_MAPPER);
    }

    public Hotel getHotelById(int id){
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
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            WHERE h.hotel_id like ?
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude, h.policy,
                     l.city_name
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


    public void deleteHotelById(int hotelId) {
        String sql = "DELETE FROM Hotels WHERE hotel_id = ?";
        jdbcTemplate.update(sql, hotelId);
    }

}
