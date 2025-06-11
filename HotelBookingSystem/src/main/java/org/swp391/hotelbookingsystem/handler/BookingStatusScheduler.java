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
            UPDATE Bookings
            SET status = 'completed'
            WHERE check_out < GETDATE()
              AND status = 'approved'
        """;

        int updated = jdbcTemplate.update(sql);
        if(updated > 0) {
            System.out.println("Auto-completed " + updated + " bookings.");
        }
    }
}
