package org.swp391.hotelbookingsystem.handler;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingStatusScheduler {

    private final JdbcTemplate jdbcTemplate;

    public BookingStatusScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 * * * * *")
    public void autoCompleteBookings() {
        String sql = """
            UPDATE BU
            SET BU.status = 'completed'
            FROM BookingUnits BU
            JOIN Bookings B ON BU.booking_id = B.booking_id
            WHERE B.check_out < GETDATE()
              AND BU.status = 'approved';
        """;

        int updated = jdbcTemplate.update(sql);
        if(updated > 0) {
            System.out.println("Auto-completed " + updated + " bookings.");
        }
    }
}
