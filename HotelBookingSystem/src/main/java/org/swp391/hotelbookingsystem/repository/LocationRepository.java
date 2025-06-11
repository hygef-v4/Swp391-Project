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
}
