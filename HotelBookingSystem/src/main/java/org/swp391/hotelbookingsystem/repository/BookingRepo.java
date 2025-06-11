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

    // 1. Lấy tất cả booking
    public List<Booking> findAll() {
        String sql = "SELECT * FROM Bookings";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class));
    }

    // 2. Tìm theo ID
    public Booking findById(int id) {
        String sql = "SELECT * FROM Bookings WHERE booking_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Booking.class), id);
    }

    // 3. Tạo booking mới
    public int save(Booking booking) {
        String sql = """
                INSERT INTO Bookings (hotel_id, room_id, customer_id, coupon_id, check_in, check_out, total_price, status, refund_amount, refund_status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(sql,
                booking.getHotelId(),
                booking.getRoomId(),
                booking.getCustomerId(),
                booking.getCouponId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getRefundAmount(),
                booking.getRefundStatus(),
                booking.getCreatedAt()
        );
    }

    // 4. Cập nhật trạng thái đặt phòng
    public int updateStatus(int bookingId, String status) {
        String sql = "UPDATE Bookings SET status = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, status, bookingId);
    }

    // 5. Cập nhật hoàn tiền
    public int updateRefund(int bookingId, double refundAmount, String refundStatus) {
        String sql = "UPDATE Bookings SET refund_amount = ?, refund_status = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, refundAmount, refundStatus, bookingId);
    }

    // 6. Xóa theo ID
    public int deleteById(int id) {
        String sql = "DELETE FROM Bookings WHERE booking_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 7. Lấy danh sách upcoming booking
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

    // 8. Lấy danh sách completed booking
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

    // 9. Lấy danh sách cancelled hoặc rejected
    public List<Booking> findCancelledBookings(int customerId) {
        String sql = """
            SELECT * FROM Bookings
            WHERE customer_id = ?
              AND (status = 'cancelled' OR status = 'rejected')
            ORDER BY created_at DESC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    // 10. Lấy ảnh phòng đầu tiên từ booking
    public String getImagesByBookingId(int bookingId) {
        String sql = """
            SELECT TOP 1 image_url
            FROM RoomImages ri
            JOIN Bookings b ON ri.room_id = b.room_id
            WHERE b.booking_id = ?
        """;
        return jdbcTemplate.queryForObject(sql, String.class, bookingId);
    }

    // 11. Lấy tên khách sạn từ booking
    public String getHotelNameByBookingId(int bookingId) {
        String sql = """
            SELECT h.hotel_name
            FROM Bookings b
            JOIN Rooms r ON b.room_id = r.room_id
            JOIN Hotels h ON r.hotel_id = h.hotel_id
            WHERE b.booking_id = ?
        """;
        return jdbcTemplate.queryForObject(sql, String.class, bookingId);
    }

}
