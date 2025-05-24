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

    // Get all locations
    public List<Location> getAllLocations() {
        String sql = "SELECT location_id AS id, city_name AS cityName, location_image_url AS imageUrl FROM Locations";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Location.class));
    }

    // Get all hotels sorted by rating DESC, including city name
    public List<Hotel> getHotelsSortedByRating() {
        String sql = """
            SELECT h.hotel_id AS hotelId,
                   h.host_id AS hostId,
                   h.hotel_name AS hotelName,
                   h.address,
                   h.description,
                   h.location_id AS locationId,
                   h.hotel_image_url AS hotelImageUrl,
                   h.rating,
                   l.city_name AS cityName
            FROM Hotels h
            JOIN Locations l ON h.location_id = l.location_id
            ORDER BY h.rating DESC
            """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Hotel.class));
    }

    // Get top 4 high-rated hotels
    public List<Hotel> getTop4HighRatedHotels() {
        String sql = """
                SELECT TOP 4 h.hotel_id AS hotelId,
                       h.host_id AS hostId,
                       h.hotel_name AS hotelName,
                       h.address,
                       h.description,
                       h.location_id AS locationId,
                       h.hotel_image_url AS hotelImageUrl,
                       h.rating,
                       l.city_name AS cityName
                FROM Hotels h
                JOIN Locations l ON h.location_id = l.location_id
                ORDER BY h.rating DESC
                """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Hotel.class));
    }
}
