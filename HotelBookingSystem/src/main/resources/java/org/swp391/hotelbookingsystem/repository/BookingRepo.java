public List<BookingUnit> findUpcomingBookingUnitsByBookingId(int bookingId) {
    String sql = """
        SELECT 
            bu.booking_unit_id,
            bu.room_id,
            bu.price,
            bu.status,
            bu.refund_amount,
            bu.refund_status,
            bu.quantity,
            r.title AS roomName,
            r.max_guests AS roomCapacity,
            (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = bu.room_id) AS imageUrl
        FROM BookingUnits bu
        JOIN Rooms r ON bu.room_id = r.room_id
        WHERE bu.booking_id = ?
        AND bu.status = 'approved'
    """;

    return jdbcTemplate.query(sql, ps -> ps.setInt(1, bookingId), (rs, rowNum) -> 
        BookingUnit.builder()
            .bookingUnitId(rs.getInt("booking_unit_id"))
            .roomId(rs.getInt("room_id"))
            .price(rs.getDouble("price"))
            .status(rs.getString("status"))
            .refundAmount(rs.getDouble("refund_amount"))
            .refundStatus(rs.getString("refund_status"))
            .quantity(rs.getInt("quantity"))
            .roomName(rs.getString("roomName"))
            .imageUrl(rs.getString("imageUrl"))
            .roomCapacity(rs.getInt("roomCapacity"))
            .build()
    );
}

public List<BookingUnit> findCompletedBookingUnitsByBookingId(int bookingId) {
    String sql = """
        SELECT 
            bu.booking_unit_id,
            bu.room_id,
            bu.price,
            bu.status,
            bu.refund_amount,
            bu.refund_status,
            bu.quantity,
            r.title AS roomName,
            r.max_guests AS roomCapacity,
            (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = bu.room_id) AS imageUrl
        FROM BookingUnits bu
        JOIN Rooms r ON bu.room_id = r.room_id
        WHERE bu.booking_id = ?
        AND bu.status = 'completed'
    """;

    return jdbcTemplate.query(sql, ps -> ps.setInt(1, bookingId), (rs, rowNum) -> 
        BookingUnit.builder()
            .bookingUnitId(rs.getInt("booking_unit_id"))
            .roomId(rs.getInt("room_id"))
            .price(rs.getDouble("price"))
            .status(rs.getString("status"))
            .refundAmount(rs.getDouble("refund_amount"))
            .refundStatus(rs.getString("refund_status"))
            .quantity(rs.getInt("quantity"))
            .roomName(rs.getString("roomName"))
            .imageUrl(rs.getString("imageUrl"))
            .roomCapacity(rs.getInt("roomCapacity"))
            .build()
    );
}

public List<BookingUnit> findCancelledBookingUnitsByBookingId(int bookingId) {
    String sql = """
        SELECT 
            bu.booking_unit_id,
            bu.room_id,
            bu.price,
            bu.status,
            bu.refund_amount,
            bu.refund_status,
            bu.quantity,
            r.title AS roomName,
            r.max_guests AS roomCapacity,
            (SELECT TOP 1 image_url FROM RoomImages WHERE room_id = bu.room_id) AS imageUrl
        FROM BookingUnits bu
        JOIN Rooms r ON bu.room_id = r.room_id
        WHERE bu.booking_id = ?
        AND (bu.status = 'cancelled' OR bu.status = 'rejected')
    """;

    return jdbcTemplate.query(sql, ps -> ps.setInt(1, bookingId), (rs, rowNum) -> 
        BookingUnit.builder()
            .bookingUnitId(rs.getInt("booking_unit_id"))
            .roomId(rs.getInt("room_id"))
            .price(rs.getDouble("price"))
            .status(rs.getString("status"))
            .refundAmount(rs.getDouble("refund_amount"))
            .refundStatus(rs.getString("refund_status"))
            .quantity(rs.getInt("quantity"))
            .roomName(rs.getString("roomName"))
            .imageUrl(rs.getString("imageUrl"))
            .roomCapacity(rs.getInt("roomCapacity"))
            .build()
    );
} 