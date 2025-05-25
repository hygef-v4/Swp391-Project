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
    private static final BeanPropertyRowMapper<Location> LOCATION_MAPPER = new BeanPropertyRowMapper<>(Location.class);

    // SQL Queries
    private static final String SELECT_ALL_LOCATIONS = """
            SELECT location_id AS id,
                   city_name AS cityName,
                   location_image_url AS imageUrl
            FROM Locations
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
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            WHERE h.location_id = ?
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
            ORDER BY h.rating DESC
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
                   MIN(r.price) AS minPrice,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            JOIN Rooms r ON h.hotel_id = r.hotel_id
            GROUP BY h.hotel_id, h.host_id, h.hotel_name, h.address, h.description,
                     h.location_id, h.hotel_image_url, h.rating, h.latitude, h.longitude,
                     l.city_name
            ORDER BY h.rating DESC
            """;

    private static final String SELECT_TOP_8_HOTELS = SELECT_HOTELS_BY_RATING.replace("SELECT", "SELECT TOP 8");

    // Methods
    public List<Location> getAllLocations() {
        return jdbcTemplate.query(SELECT_ALL_LOCATIONS, LOCATION_MAPPER);
    }

    public List<Hotel> getHotelsSortedByRating() {
        return jdbcTemplate.query(SELECT_HOTELS_BY_RATING, HOTEL_MAPPER);
    }

    public List<Hotel> getTop8HighRatedHotels() {
        return jdbcTemplate.query(SELECT_TOP_8_HOTELS, HOTEL_MAPPER);
    }

    public List<Hotel> getHotelsByLocation(int locationId) {
        return jdbcTemplate.query(SELECT_HOTEL_BY_LOCATION, ps -> {
            ps.setInt(1, locationId);
        }, HOTEL_MAPPER);
    }
}
