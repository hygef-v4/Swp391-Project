package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Booking;

import java.util.List;

@Repository
public class BookingRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Booking> findAll() {
        String sql = "SELECT * FROM Bookings";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class));
    }

    public Booking findById(int id) {
        String sql = "SELECT * FROM Bookings WHERE booking_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Booking.class), id);
    }

    public int updateStatus(int bookingId, String status) {
        String sql = "UPDATE Bookings SET status = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, status, bookingId);
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM Bookings WHERE booking_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int save(Booking booking) {
        String sql = """
                INSERT INTO Bookings (room_id, customer_id, coupon_id, check_in, check_out, total_price, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(sql,
                booking.getRoomId(),
                booking.getCustomerId(),
                booking.getCouponId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }

    public List<Booking> findUpcomingBookings(int customerId) {
        String sql = """
            SELECT * FROM Bookings
            WHERE customer_id = ?
              AND check_in > GETDATE()
              AND status = 'approved'
            ORDER BY check_in ASC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    public List<Booking> findCompletedBookings(int customerId) {
        String sql = """
            SELECT * FROM Bookings
            WHERE customer_id = ?
              AND check_out < GETDATE()
              AND status = 'approved'
            ORDER BY check_out DESC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    public List<Booking> findCancelledBookings(int customerId) {
        String sql = """
            SELECT * FROM Bookings
            WHERE customer_id = ?
              AND (status = 'cancelled' OR status = 'rejected')
            ORDER BY created_at DESC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    public String getImagesByBookingId(int bookingId) {
        String sql = "select top 1 image_url  from RoomImages ri join Bookings b on ri.room_id = b.room_id where booking_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, bookingId);
    }

    public String getHotelNameByBookingId(int bookingId) {
        String sql = "SELECT h.hotel_name AS hotel_name " +
                "FROM Bookings b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Hotels h ON r.hotel_id = h.hotel_id " +
                "WHERE b.booking_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, bookingId);
    }


    public List<Booking> searchByKeyword(String keyword) {
        String sql = "SELECT * FROM Bookings WHERE Bookings.check_in LIKE ? OR Bookings.customer_id LIKE ? OR Bookings.status LIKE ?";
        String q = "%" + keyword + "%";
        return jdbcTemplate.query(sql, new Object[]{q, q, q}, new BeanPropertyRowMapper<>(Booking.class));
    }
}
