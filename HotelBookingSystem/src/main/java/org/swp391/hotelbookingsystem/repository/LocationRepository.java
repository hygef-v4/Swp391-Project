package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Location;

import java.util.List;

@Repository
public class LocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Location> getAllLocations() {
        String sql = """
                    SELECT 
                        location_id AS id,
                        city_name AS cityName,
                        location_image_url AS imageUrl
                    FROM Locations
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Location.class));
    }

    public List<Location> getLocation(int locationId) {
        String sql = """
                    SELECT
                        location_id AS id,
                        city_name AS cityName,
                        location_image_url AS imageUrl
                    FROM Locations
                    WHERE location_id = ?
                """;
        return jdbcTemplate.query(sql, ps -> {
            ps.setInt(1, locationId);
        }, new BeanPropertyRowMapper<>(Location.class));
    }

    public List<Location> getAllLocationStats() {
        String sql = """
        SELECT 
            l.location_id AS id,
            l.city_name AS cityName,
            l.location_image_url AS imageUrl,
            COUNT(DISTINCT h.hotel_id) AS numberOfHotels,
            COUNT(r.room_id) AS totalRooms,
            AVG(h.rating) AS averageRating
        FROM Locations l
        LEFT JOIN Hotels h ON l.location_id = h.location_id
        LEFT JOIN Rooms r ON h.hotel_id = r.hotel_id
        GROUP BY l.location_id, l.city_name, l.location_image_url
        ORDER BY numberOfHotels DESC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Location.class));
    }

    public void insertLocation(Location location) {
        String sql = "INSERT INTO Locations (city_name, location_image_url) VALUES (?, ?)";
        jdbcTemplate.update(sql, location.getCityName(), location.getImageUrl());
    }

    public boolean cityNameExistsIgnoreCase(String cityName) {
        String sql = "SELECT COUNT(*) FROM Locations WHERE LOWER(city_name) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cityName.trim());
        return count != null && count > 0;
    }

}
