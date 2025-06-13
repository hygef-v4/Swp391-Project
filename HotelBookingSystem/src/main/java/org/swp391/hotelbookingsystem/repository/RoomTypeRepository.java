package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.RoomTypes;


import java.util.List;

@Repository
public class RoomTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomTypes> getAllRoomTypes() {
        String sql = """
                    SELECT 
                        room_type_id AS roomTypeId,
                        name,
                        description
                    FROM RoomTypes
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RoomTypes.class));
    }

    public RoomTypes getRoomTypeById(int roomTypeId) {
        String sql = """
                    SELECT 
                        room_type_id AS roomTypeId,
                        name,
                        description
                    FROM RoomTypes
                    WHERE room_type_id = ?
                """;
        return jdbcTemplate.queryForObject(sql,
                new BeanPropertyRowMapper<>(RoomTypes.class),
                roomTypeId);
    }
}
