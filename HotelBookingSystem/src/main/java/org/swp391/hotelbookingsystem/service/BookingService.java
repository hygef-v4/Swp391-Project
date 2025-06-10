package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.repository.BookingRepo;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepo bookingRepo;

    public void cancelBooking(int id) {
        bookingRepo.updateStatus(id, "cancelled");
    }

    public List<Booking> getUpcomingBookings(int customerId) {
        return bookingRepo.findUpcomingBookings(customerId);
    }
    public List<Booking> getCompletedBookings(int customerId) {
        return bookingRepo.findCompletedBookings(customerId);
    }
    public List<Booking> getCancelledBookings(int customerId) {
        return bookingRepo.findCancelledBookings(customerId);
    }
    public Booking findById(int id) {
        return bookingRepo.findById(id);
    }

    public String getImageByBookingId(int bookingId) {
        return bookingRepo.getImagesByBookingId(bookingId);
    }

    public String getHotelNameByBookingId(int bookingId) {
        return bookingRepo.getHotelNameByBookingId(bookingId);
    }

    public void updateStatus(Booking booking, String status) {
        bookingRepo.updateStatus(booking.getBookingId(), status);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public List<Booking> searchBookings(String keyword) {
        return bookingRepo.searchByKeyword(keyword);
    }

}
