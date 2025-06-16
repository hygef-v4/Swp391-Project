package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Booking;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookingRepo {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Lấy tất cả booking
    public List<Booking> findAll() {
        String sql = """
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
    """;
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
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE b.customer_id = ?
          AND b.check_in > GETDATE()
          AND b.status = 'approved'
        ORDER BY b.check_in ASC
    """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    // 8. Lấy danh sách completed booking
    public List<Booking> findCompletedBookings(int customerId) {
        String sql = """
            SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.customer_id = ?
              AND b.check_out < GETDATE()
              AND (b.status = 'completed' OR b.status = 'approved')
            ORDER BY b.check_out DESC
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), customerId);
    }

    // 9. Lấy danh sách cancelled hoặc rejected
    public List<Booking> findCancelledBookings(int customerId) {
        String sql = """
            SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.customer_id = ?
              AND (b.status = 'cancelled' OR b.status = 'rejected')
            ORDER BY b.created_at DESC
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

    // 12. Tìm kiếm theo keyword
    public List<Booking> searchByKeyword(String keyword) {
        String q = "%" + keyword + "%";
        String sql = """
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE CAST(b.check_in AS NVARCHAR) LIKE ?
           OR CAST(b.customer_id AS NVARCHAR) LIKE ?
           OR b.status LIKE ?
    """;
        return jdbcTemplate.query(sql, new Object[]{q, q, q}, new BeanPropertyRowMapper<>(Booking.class));
    }

    // 13. Lấy booking theo trạng thái hoàn tiền
    public List<Booking> findByRefundStatus(String refundStatus) {
        String sql = """
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE b.refund_status = ?
    """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), refundStatus);
    }

    // 14. Lấy booking theo khách sạn
    public List<Booking> findByHotelId(int hotelId) {
        String sql = """
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE b.hotel_id = ?
    """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), hotelId);
    }

    // 15. Lấy tên loại phòng từ booking
    public String getRoomNameByBookingId(int bookingId) {
        String sql = """
            SELECT r.title
            FROM Bookings b
            JOIN Rooms r ON b.room_id = r.room_id
            WHERE b.booking_id = ?
        """;
        return jdbcTemplate.queryForObject(sql, String.class, bookingId);
    }

    public record DailyStat(String date, int count) {

    }

    public List<DailyStat> getCheckInStats() {
        String sql = """
                    SELECT CONVERT(VARCHAR, CAST(check_in AS DATE), 23) AS date, COUNT(*) AS count
                    FROM Bookings
                    WHERE status = 'approved'
                    GROUP BY CAST(check_in AS DATE)
                    ORDER BY CAST(check_in AS DATE)
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)
                -> new DailyStat(rs.getString("date"), rs.getInt("count"))
        );
    }

    public List<DailyStat> getCheckOutStats() {
        String sql = """
                    SELECT CONVERT(VARCHAR, CAST(check_out AS DATE), 23) AS date, COUNT(*) AS count
                    FROM Bookings
                    WHERE status = 'approved'
                    GROUP BY CAST(check_out AS DATE)
                    ORDER BY CAST(check_out AS DATE)
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)
                -> new DailyStat(rs.getString("date"), rs.getInt("count"))
        );
    }

    public int getTotalBookingByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    public int getTodayCheckIn() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings 
        WHERE status = 'approved'
          AND CAST(check_in AS DATE) = CAST(GETDATE() AS DATE)
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getTodayBookingByStatus(String status) {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings 
        WHERE status = ? 
          AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    public int getTodayCheckOut() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings 
        WHERE status = 'approved'
          AND CAST(check_out AS DATE) = CAST(GETDATE() AS DATE)
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getFutureCheckOut() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings 
        WHERE status = 'approved'
          AND CAST(check_out AS DATE) > CAST(GETDATE() AS DATE)
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getFutureCheckIn() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings 
        WHERE status = 'approved'
          AND CAST(check_in AS DATE) > CAST(GETDATE() AS DATE)
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Booking> findAllPaginated(int page, int size) {
        int offset = page * size;
        String sql = """
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        ORDER BY b.created_at DESC
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booking.class), offset, size);
    }

    public int countAllBookings() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Bookings", Integer.class);
    }

    public List<Booking> findBookingsByStatusPaginated(String status, int page, int size) {
        StringBuilder sql = new StringBuilder("""
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
    """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" WHERE b.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page * size);
        params.add(size);

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Booking.class), params.toArray());
    }


    public int countBookingsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Bookings";
        if (status != null && !status.isBlank()) {
            sql += " WHERE status = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, status);
        }
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Booking> findBookingsByStatusAndKeywordPaginated(String status, String keyword, int page, int size) {
        StringBuilder sql = new StringBuilder("""
        SELECT b.*, 
               h.hotel_name AS hotelName,
               r.title AS roomName,
               (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = b.room_id) AS imageUrl
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR h.hotel_name LIKE ?)");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        sql.append(" ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page * size);
        params.add(size);

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Booking.class), params.toArray());
    }

    public int countBookingsByStatusAndKeyword(String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM Bookings b
        JOIN Rooms r ON b.room_id = r.room_id
        JOIN Hotels h ON b.hotel_id = h.hotel_id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR h.hotel_name LIKE ?)");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

}
