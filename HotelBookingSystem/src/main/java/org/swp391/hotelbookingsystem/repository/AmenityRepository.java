package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.AmenityCategory;

import java.util.List;

@Repository
public class AmenityRepository {

    private final JdbcTemplate jdbcTemplate;

    public AmenityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Amenity> getAllAmenities() {
        String sql = """
                    SELECT 
                        amenity_id AS amenityId,
                        name,
                        category_id AS categoryId
                    FROM Amenities
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Amenity.class));
    }

    public List<Amenity> getAmenitiesByCategoryId(int categoryId) {
        String sql = """
                    SELECT 
                        amenity_id AS amenityId,
                        name,
                        category_id AS categoryId
                    FROM Amenities
                    WHERE category_id = ?
                """;
        return jdbcTemplate.query(sql, ps -> {
            ps.setInt(1, categoryId);
        }, new BeanPropertyRowMapper<>(Amenity.class));
    }

    public List<Amenity> getAllAmenitiesWithCategory() {
        String sql = """
        SELECT 
            a.amenity_id,
            a.name AS amenity_name,
            a.category_id,
            c.name AS category_name
        FROM Amenities a
        JOIN AmenityCategories c ON a.category_id = c.category_id
        ORDER BY c.category_id
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AmenityCategory category = new AmenityCategory();
            category.setCategoryId(rs.getInt("category_id"));
            category.setName(rs.getString("category_name"));

            Amenity amenity = new Amenity();
            amenity.setAmenityId(rs.getInt("amenity_id"));
            amenity.setName(rs.getString("amenity_name"));
            amenity.setCategoryId(rs.getInt("category_id"));
            amenity.setCategory(category);

            return amenity;
        });
    }

    public List<Amenity> getRoomAmenities(int roomId) {
        String sql = """
            SELECT 
                a.amenity_id,
                a.name AS amenity_name,
                a.category_id,
                ar.room_id,
                c.name AS category_name
            FROM Amenities a
            JOIN AmenityCategories c ON a.category_id = c.category_id
            JOIN RoomAmenities ar on a.amenity_id = ar.amenity_id
            WHERE ar.room_id = ?
            ORDER BY c.category_id
        """;

        return jdbcTemplate.query(sql, ps -> ps.setInt(1, roomId), (rs, rowNum) -> {
            AmenityCategory category = new AmenityCategory();
            category.setCategoryId(rs.getInt("category_id"));
            category.setName(rs.getString("category_name"));

            Amenity amenity = new Amenity();
            amenity.setAmenityId(rs.getInt("amenity_id"));
            amenity.setName(rs.getString("amenity_name"));
            amenity.setCategoryId(rs.getInt("category_id"));
            amenity.setRoomId(rs.getInt("room_id"));
            amenity.setCategory(category);

            return amenity;
        });
    }
}
