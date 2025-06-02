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


}
