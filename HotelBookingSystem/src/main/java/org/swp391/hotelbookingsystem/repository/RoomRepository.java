package org.swp391.hotelbookingsystem.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Room;

@Repository
public class RoomRepository {
    final
    JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Room> ROOM_MAPPER = new BeanPropertyRowMapper<>(Room.class);

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getRoomImages(int id) {
        String query = "select image_url from RoomImages where room_id = ?";
        return jdbcTemplate.queryForList(query, String.class, id);
    }

    public int insertRoom(Room room) {
        String sql = """
                    INSERT INTO Rooms (hotel_id, title, description, price, max_guests, room_type_id, quantity, status)
                    OUTPUT INSERTED.room_id
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.queryForObject(sql, Integer.class,
                room.getHotelId(),
                room.getTitle(),
                room.getDescription(),
                room.getPrice(),
                room.getMaxGuests(),
                room.getRoomTypeId(),
                room.getQuantity(),
                room.getStatus() != null ? room.getStatus() : "active"
        );
    }

    public void insertRoomImage(int roomId, String imageUrl) {
        String sql = "INSERT INTO RoomImages (room_id, image_url) VALUES (?, ?)";
        jdbcTemplate.update(sql, roomId, imageUrl);
    }

    public void linkRoomAmenity(int roomId, int amenityId) {
        String sql = "INSERT INTO RoomAmenities (room_id, amenity_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, roomId, amenityId);
    }

    public List<Room> getRoomsByHotelId(int hotelId) {
        String sql = """
                    SELECT 
                        room_id AS roomId,
                        hotel_id AS hotelId,
                        title,
                        description,
                        price,
                        max_guests AS maxGuests,
                        room_type_id AS roomTypeId,
                        status,
                        quantity
                    FROM Rooms
                    WHERE hotel_id = ?
                """;
        return jdbcTemplate.query(sql, ROOM_MAPPER, hotelId);
    }

    public List<Room> getAvailableRoomsByHotelId(int hotelId) {
        String sql = """
                    SELECT 
                        room_id AS roomId,
                        hotel_id AS hotelId,
                        title,
                        description,
                        price,
                        max_guests AS maxGuests,
                        room_type_id AS roomTypeId,
                        status,
                        quantity
                    FROM Rooms
                    WHERE hotel_id = ? AND quantity > 0
                """;
        return jdbcTemplate.query(sql, ROOM_MAPPER, hotelId);
    }

    private static final String COUNT_ROOMS = "SELECT SUM(quantity) FROM Rooms";

    public int countRooms() {
        return jdbcTemplate.queryForObject(COUNT_ROOMS, Integer.class);
    }


    public int getTotalRoomsByHostId(int hostId) {
        String sql = """
                SELECT SUM(r.quantity)
                FROM Rooms r
                JOIN Hotels h ON r.hotel_id = h.hotel_id
                WHERE h.host_id = ?
            """;
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class, hostId);
        return total != null ? total : 0; // Handle null in case no rooms exist
    }

    public int countBookedRoomsByHostId(int hostId) {
        String sql = """
            SELECT COUNT(bu.booking_unit_id)
            FROM BookingUnits bu
            JOIN Bookings b ON bu.booking_id = b.booking_id
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE h.host_id = ? AND bu.status = 'approved'
        """;
        try {
            Integer total = jdbcTemplate.queryForObject(sql, Integer.class, hostId);
            return total != null ? total : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void updateRoom(Room room) {
        String sql = """
                UPDATE Rooms 
                SET title = ?, 
                    description = ?, 
                    price = ?, 
                    max_guests = ?, 
                    room_type_id = ?, 
                    quantity = ?, 
                    status = ?
                WHERE room_id = ?
            """;
        jdbcTemplate.update(sql,
                room.getTitle(),
                room.getDescription(),
                room.getPrice(),
                room.getMaxGuests(),
                room.getRoomTypeId(),
                room.getQuantity(),
                room.getStatus(),
                room.getRoomId()
        );
    }

    public void clearRoomAmenities(int roomId) {
        String sql = "DELETE FROM RoomAmenities WHERE room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }

    public void clearRoomImages(int roomId) {
        String sql = "DELETE FROM RoomImages WHERE room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }

    public void deleteRoom(int roomId) {
        // Delete related data first (foreign key constraints)
        clearRoomAmenities(roomId);
        clearRoomImages(roomId);
        
        // Delete room itself
        String sql = "DELETE FROM Rooms WHERE room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }

    public int countAvailableRoomsByHotelId(int hotelId) {
        String sql = """
        SELECT SUM(quantity) FROM Rooms
        WHERE hotel_id = ? AND status = 'available'
    """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, hotelId);
        return result != null ? result : 0;
    }

}
