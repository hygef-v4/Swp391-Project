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

    @Scheduled(cron = "0 * * * * *")
    public void deletePending(){
        List<Integer> id = bookingRepo.isPendingOverTIme();
        for(int i : id){
            bookingRepo.deletePendingBooking(i);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void autoUpdateBookingStatuses() {
        int checkInUpdated = bookingRepo.autoUpdateCheckin();
        int completedUpdated = bookingRepo.autoUpdateCompleted();

        if (checkInUpdated > 0 || completedUpdated > 0) {
            System.out.printf("Booking status updated: %d to check_in, %d to completed%n", checkInUpdated, completedUpdated);
        }
    }
}
