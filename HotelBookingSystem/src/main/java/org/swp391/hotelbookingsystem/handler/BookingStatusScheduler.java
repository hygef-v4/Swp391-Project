package org.swp391.hotelbookingsystem.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swp391.hotelbookingsystem.repository.BookingRepo;

@Component
public class BookingStatusScheduler {
    @Autowired
    private BookingRepo bookingRepo;

    private final JdbcTemplate jdbcTemplate;

    public BookingStatusScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 * * * * *")
    public void deletePending(){
        List<Integer> id = bookingRepo.isPendingOverTIme();
        for(int i : id){
            bookingRepo.deletePendingBooking(i);
        }
    }

    @Scheduled(cron = "0 * * * * *") // Mỗi phút
    public void autoUpdateBookingStatuses() {
        // 1. Cập nhật từ approved -> check_in nếu đang trong khoảng thời gian check-in
        String updateCheckInSql = """
            UPDATE BU
            SET BU.status = 'check_in'
            FROM BookingUnits BU
            JOIN Bookings B ON BU.booking_id = B.booking_id
            WHERE B.check_in <= CAST(GETDATE() AS DATE)
              AND B.check_out >= CAST(GETDATE() AS DATE)
              AND BU.status = 'approved';
        """;

        // 2. Cập nhật từ approved|check_in -> completed nếu check_out < hôm nay
        String updateCompletedSql = """
            UPDATE BU
            SET BU.status = 'completed'
            FROM BookingUnits BU
            JOIN Bookings B ON BU.booking_id = B.booking_id
            WHERE B.check_out < CAST(GETDATE() AS DATE)
              AND BU.status = 'check_in';
        """;

        int checkInUpdated = jdbcTemplate.update(updateCheckInSql);
        int completedUpdated = jdbcTemplate.update(updateCompletedSql);

        if (checkInUpdated > 0 || completedUpdated > 0) {
            System.out.printf("Booking status updated: %d to check_in, %d to completed%n", checkInUpdated, completedUpdated);
        }
    }
}
