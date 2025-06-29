package org.swp391.hotelbookingsystem.repository;

import java.sql.Date;
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

    public List<Room> getRoomsByIdAndDateRange(int id, Date checkin, Date checkout){
        String sql = """
                    WITH BookedRooms AS (
                        SELECT 
                            bu.room_id,
                            SUM(bu.quantity) AS booked_quantity
                        FROM BookingUnits bu
                        JOIN Bookings b ON b.booking_id = bu.booking_id
                        WHERE bu.status IN ('approved', 'check_in')
                            AND b.check_out >= ? AND b.check_in <= ?
                        GROUP BY bu.room_id
                    )
                    SELECT 
                        Rooms.room_id AS roomId,
                        hotel_id AS hotelId,
                        title,
                        description,
                        price,
                        max_guests AS maxGuests,
                        room_type_id AS roomTypeId,
                        status,
                        quantity - ISNULL(booked_quantity, 0) AS quantity
                    FROM Rooms 
                    LEFT JOIN BookedRooms ON Rooms.room_id = BookedRooms.room_id
                    WHERE hotel_id = ?
                """;
        return jdbcTemplate.query(sql, ps -> {
            ps.setDate(1, checkin != null ? checkin : new Date(System.currentTimeMillis()));
            ps.setDate(2, checkout != null ? checkout : new Date(System.currentTimeMillis()));
            ps.setInt(3, id);
        }, ROOM_MAPPER);
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
                        quantity - ISNULL((SELECT SUM(quantity) FROM BookingUnits WHERE room_id = Rooms.room_id AND status LIKE 'approved'), 0) AS quantity
                    FROM Rooms
                    WHERE hotel_id = ?
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
        
        // 1. Delete only cancelled/rejected booking units that reference this room
        String deleteBookingUnitsSql = "DELETE FROM BookingUnits WHERE room_id = ? AND status IN ('cancelled', 'rejected')";
        jdbcTemplate.update(deleteBookingUnitsSql, roomId);
        
        // 2. Delete room amenities
        clearRoomAmenities(roomId);
        
        // 3. Delete room images
        clearRoomImages(roomId);
        
        // 4. Delete room itself
        String sql = "DELETE FROM Rooms WHERE room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }

    public Room getRoomById(int roomId) {
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
        WHERE room_id = ?
    """;

        try {
            Room room = jdbcTemplate.queryForObject(sql, ROOM_MAPPER, roomId);
            List<String> images = getRoomImages(roomId);
            room.setImages(images);

            return room;
        } catch (Exception e) {
            return null;
        }
    }

    public int countAvailableRoomsByHotelId(int hotelId) {
        String sql = """
        SELECT SUM(quantity) FROM Rooms
        WHERE hotel_id = ? AND status = 'available'
    """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, hotelId);
        return result != null ? result : 0;
    }

    public boolean hasActiveBookingUnits(int roomId) {
        String sql = """
            SELECT COUNT(*) FROM BookingUnits 
            WHERE room_id = ? AND status IN ('approved')
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId);
        return count != null && count > 0;
    }

}
