package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Location;

import java.util.List;

@Repository
public class LocationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Location> getAllLocations() {
        String sql = "SELECT * FROM Locations";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Location location = new Location();
            location.setId(rs.getInt("location_id"));
            location.setCityName(rs.getString("city_name"));
            location.setImageUrl(rs.getString("location_image_url"));
            return location;
        });
    }


}
