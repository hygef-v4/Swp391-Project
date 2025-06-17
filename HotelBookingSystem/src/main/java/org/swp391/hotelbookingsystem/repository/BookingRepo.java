package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookingRepo {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BookingUnit> findBookingUnitsByBookingId(int bookingId) {
        String sql = """
            SELECT 
                bu.booking_unit_id,
                bu.room_id,
                bu.price,
                bu.status,
                bu.refund_amount,
                bu.refund_status,
                r.title AS roomName,
                (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = bu.room_id) AS imageUrl
            FROM BookingUnits bu
            JOIN Rooms r ON bu.room_id = r.room_id
            WHERE bu.booking_id = ?
        """;

        return jdbcTemplate.query(sql, ps -> ps.setInt(1, bookingId), (rs, rowNum) -> 
            BookingUnit.builder()
                .bookingUnitId(rs.getInt("booking_unit_id"))
                .roomId(rs.getInt("room_id"))
                .price(rs.getDouble("price"))
                .status(rs.getString("status"))
                .refundAmount(rs.getDouble("refund_amount"))
                .refundStatus(rs.getString("refund_status"))
                .roomName(rs.getString("roomName"))
                .imageUrl(rs.getString("imageUrl"))
                .build()
        );
    }

    public BookingUnit findBookingUnitById(int id) {
        String sql = """            
            SELECT 
                bu.booking_unit_id,
                bu.room_id,
                bu.price,
                bu.status,
                bu.refund_amount,
                bu.refund_status,
                r.title AS roomName,
                (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = bu.room_id) AS imageUrl
            FROM BookingUnits bu
            JOIN Rooms r ON bu.room_id = r.room_id
            WHERE booking_unit_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            BookingUnit unit = new BookingUnit();
            unit.setBookingUnitId(rs.getInt("booking_unit_id"));
            unit.setRoomId(rs.getInt("room_id"));
            unit.setPrice(rs.getDouble("price"));
            unit.setStatus(rs.getString("status"));
            unit.setRefundAmount(rs.getDouble("refund_amount"));
            unit.setRefundStatus(rs.getString("refund_status"));
            unit.setRoomName(rs.getString("roomName"));
            unit.setImageUrl(rs.getString("imageUrl"));
            return unit;
        }, id);
    }

    public Booking findBookingByBookingUnitId(int bookingUnitId) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE booking_id = (SELECT booking_id FROM BookingUnits WHERE booking_unit_id = ?)
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Booking booking = new Booking();
            booking.setBookingId(rs.getInt("booking_id"));
            booking.setHotelId(rs.getInt("hotel_id"));
            booking.setCustomerId(rs.getInt("customer_id"));
            booking.setCouponId((Integer) rs.getObject("coupon_id"));
            booking.setCheckIn(rs.getTimestamp("check_in").toLocalDateTime());
            booking.setCheckOut(rs.getTimestamp("check_out").toLocalDateTime());
            booking.setTotalPrice(rs.getDouble("total_price"));
            booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            booking.setHotelName(rs.getString("hotel_name"));
            booking.setImageUrl(rs.getString("hotel_image_url"));
            return booking;
        }, bookingUnitId);
    }

    // 1. Lấy tất cả booking
    public List<Booking> findAll() {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        });
    }

    // 2. Tìm theo ID
    public Booking findById(int id) {
        String sql = """            
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE booking_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, id);
    }

    // 3. Tạo booking mới
    // public int save(Booking booking) {
    //     String sql = """
    //             INSERT INTO Bookings (hotel_id, customer_id, coupon_id, check_in, check_out, total_price)
    //             VALUES (?, ?, ?, ?, ?, ?)
    //             """;

    //     return jdbcTemplate.update(sql,
    //             booking.getHotelId(),
    //             booking.getRoomId(),
    //             booking.getCustomerId(),
    //             booking.getCouponId(),
    //             booking.getCheckIn(),
    //             booking.getCheckOut(),
    //             booking.getTotalPrice(),
    //             booking.getStatus(),
    //             booking.getRefundAmount(),
    //             booking.getRefundStatus(),
    //     );
    // }

    // 4. Cập nhật trạng thái đặt phòng
    public int updateStatus(int bookingUnitId, String status) {
        String sql = "UPDATE BookingUnits SET status = ? WHERE booking_unit_id = ?";
        return jdbcTemplate.update(sql, status, bookingUnitId);
    }

    // 5. Cập nhật hoàn tiền
    public int updateRefund(int bookingUnitId, double refundAmount, String refundStatus) {
        String sql = "UPDATE BookingUnits SET refund_amount = ?, refund_status = ? WHERE booking_unit_id = ?";
        return jdbcTemplate.update(sql, refundAmount, refundStatus, bookingUnitId);
    }

    // 6. Xóa theo ID
    public int deleteById(int id) {
        String sql = "DELETE FROM BookingUnits WHERE booking_unit_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 7. Lấy danh sách upcoming booking
    public List<Booking> findUpcomingBookings(int customerId) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.customer_id = ?
            AND b.check_in > GETDATE()
            AND EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND bu.status = 'approved'
            )
            ORDER BY b.check_in ASC;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, customerId);
    }

    // 8. Lấy danh sách completed booking
    public List<Booking> findCompletedBookings(int customerId) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.customer_id = ?
            AND b.check_in < GETDATE()
            AND EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND (bu.status = 'completed' OR bu.status = 'approved')
            )
            ORDER BY b.check_in ASC;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, customerId);
    }

    // 9. Lấy danh sách cancelled hoặc rejected
    public List<Booking> findCancelledBookings(int customerId) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.customer_id = ?
            AND EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND (bu.status = 'cancelled' OR bu.status = 'rejected')
            )
            ORDER BY b.check_in ASC;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, customerId);
    }

    // 10. Tìm kiếm theo keyword
    public List<Booking> searchByKeyword(String keyword) {
        String q = "%" + keyword + "%";

        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE CAST(b.check_in AS NVARCHAR) LIKE ?
                OR CAST(b.customer_id AS NVARCHAR) LIKE ?
                OR EXISTS (
                    SELECT 1
                    FROM BookingUnits bu
                    WHERE bu.booking_id = b.booking_id
                        AND bu.status = ?
                )
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, q, q, q);
    }

    // 11. Lấy booking theo trạng thái hoàn tiền
    public List<Booking> findByRefundStatus(String refundStatus) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND bu.refund_status = ?
            )
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, refundStatus);
    }

    // 12. Lấy booking theo khách sạn
    public List<Booking> findByHotelId(int hotelId) {
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE b.hotel_id = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, hotelId);
    }

    public record DailyStat(String date, int count) {

    }

    public List<DailyStat> getCheckInStats() {
        String sql = """
                    SELECT CONVERT(VARCHAR, CAST(check_in AS DATE), 23) AS date, COUNT(*) AS count
                    FROM Bookings b
                    WHERE EXISTS (
                        SELECT 1
                        FROM BookingUnits bu
                        WHERE bu.booking_id = b.booking_id
                            AND bu.status = 'approved'
                    )
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
                    FROM Bookings b
                    WHERE EXISTS (
                        SELECT 1
                        FROM BookingUnits bu
                        WHERE bu.booking_id = b.booking_id
                            AND bu.status = 'approved'
                    )
                    GROUP BY CAST(check_out AS DATE)
                    ORDER BY CAST(check_out AS DATE)
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)
                -> new DailyStat(rs.getString("date"), rs.getInt("count"))
        );
    }

    public int getTotalBookingByStatus(String status) {
        String sql = """
            SELECT COUNT(*) 
            FROM Bookings b
            WHERE EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND bu.status = ?
            )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    public int getTodayBookingByStatus(String status) {
        String sql = """
            SELECT COUNT(*) 
            FROM Bookings b
            WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)
            AND EXISTS (
                SELECT 1
                FROM BookingUnits bu
                WHERE bu.booking_id = b.booking_id
                    AND bu.status = ?
            )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    public int getTodayCheckIn() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings b
        WHERE CAST(check_in AS DATE) = CAST(GETDATE() AS DATE)
        AND EXISTS (
            SELECT 1
            FROM BookingUnits bu
            WHERE bu.booking_id = b.booking_id
                AND bu.status = 'approved'
        )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getFutureCheckIn() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings b
        WHERE CAST(check_in AS DATE) > CAST(GETDATE() AS DATE)
        AND EXISTS (
            SELECT 1
            FROM BookingUnits bu
            WHERE bu.booking_id = b.booking_id
                AND bu.status = 'approved'
        )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getTodayCheckOut() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings b
        WHERE CAST(check_out AS DATE) = CAST(GETDATE() AS DATE)
        AND EXISTS (
            SELECT 1
            FROM BookingUnits bu
            WHERE bu.booking_id = b.booking_id
                AND bu.status = 'approved'
        )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int getFutureCheckOut() {
        String sql = """
        SELECT COUNT(*) 
        FROM Bookings b
        WHERE CAST(check_out AS DATE) > CAST(GETDATE() AS DATE)
        AND EXISTS (
            SELECT 1
            FROM BookingUnits bu
            WHERE bu.booking_id = b.booking_id
                AND bu.status = 'approved'
        )
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Booking> findAllPaginated(int page, int size) {
        int offset = page * size;
        String sql = """
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            ORDER BY b.created_at DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, offset, size);
    }

    public int countAllBookings() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Bookings", Integer.class);
    }

    public List<Booking> findBookingsByStatusPaginated(String status, int page, int size) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" WHERE EXISTS (SELECT 1 FROM BookingUnits bu WHERE bu.booking_id = b.booking_id AND bu.status = ?)");
            params.add(status);
        }

        sql.append(" ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page * size);
        params.add(size);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, params.toArray());
    }

    public int countBookingsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Bookings b";
        if (status != null && !status.isBlank()) {
            sql += " WHERE EXISTS (SELECT 1 FROM BookingUnits bu WHERE bu.booking_id = b.booking_id AND bu.status = ?)";
            return jdbcTemplate.queryForObject(sql, Integer.class, status);
        }return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Booking> findBookingsByStatusAndKeywordPaginated(String status, String keyword, int page, int size) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                b.booking_id,
                b.hotel_id,
                b.customer_id,
                b.coupon_id,
                b.check_in,
                b.check_out,
                b.total_price,
                b.created_at,
                h.hotel_name,
                h.hotel_image_url
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM BookingUnits bu WHERE bu.booking_id = b.booking_id AND bu.status = ?)");
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM BookingUnits bu JOIN Rooms r ON bu.room_id = r.room_id WHERE bu.booking_id = b.booking_id AND (r.title LIKE ? OR h.hotel_name LIKE ?))");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        sql.append(" ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page * size);
        params.add(size);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            int bookingId = rs.getInt("booking_id");

            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .hotelId(rs.getInt("hotel_id"))
                    .customerId(rs.getInt("customer_id"))
                    .couponId((Integer) rs.getObject("coupon_id"))
                    .checkIn(rs.getTimestamp("check_in").toLocalDateTime())
                    .checkOut(rs.getTimestamp("check_out").toLocalDateTime())
                    .totalPrice(rs.getDouble("total_price"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .hotelName(rs.getString("hotel_name"))
                    .imageUrl(rs.getString("hotel_image_url"))
                    .build();

            // Gán BookingUnit cho từng Booking
            booking.setBookingUnits(findBookingUnitsByBookingId(bookingId));

            return booking;
        }, params.toArray());
    }

    public int countBookingsByStatusAndKeyword(String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM Bookings b
            JOIN Hotels h ON b.hotel_id = h.hotel_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM BookingUnits bu WHERE bu.booking_id = b.booking_id AND bu.status = ?)");
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM BookingUnits bu JOIN Rooms r ON bu.room_id = r.room_id WHERE bu.booking_id = b.booking_id AND (r.title LIKE ? OR h.hotel_name LIKE ?))");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

}
