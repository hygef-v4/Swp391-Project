package org.swp391.hotelbookingsystem.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Room;

@Repository
public class RoomRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Room> ROOM_MAPPER = new BeanPropertyRowMapper<>(Room.class);

    public List<Room> getRoomByHotelId(int id){
        String query = """
select 
    room_id as roomId,
    hotel_id as hotelId,
    title,
    Rooms.description,
    price,
    max_guests as maxGuest,
    status,
    quantity,
    name as roomType
from Rooms
join RoomTypes on Rooms.room_type_id = RoomTypes.room_type_id
where hotel_id = ?
                """;
        return jdbcTemplate.query(query, ROOM_MAPPER, id);
    }

    public List<String> getRoomImages(int id){
        String query = "select image_url from RoomImages where room_id = ?";
        return jdbcTemplate.queryForList(query, String.class, id);
    }
}
