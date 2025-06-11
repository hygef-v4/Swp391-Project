package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.repository.BookingRepo;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepo bookingRepo;

    // Hủy đơn đặt phòng
    public void cancelBooking(int id) {
        bookingRepo.updateStatus(id, "cancelled");
    }

    // Đặt lại trạng thái
    public void updateStatus(Booking booking, String status) {
        bookingRepo.updateStatus(booking.getBookingId(), status);
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

    // Cập nhật hoàn tiền
    public void updateRefund(int bookingId, Double amount, String status) {
        bookingRepo.updateRefund(bookingId, amount, status);
    }

    // Lấy ảnh và tên khách sạn
    public String getImageByBookingId(int bookingId) {
        return bookingRepo.getImagesByBookingId(bookingId);
    }

    public String getHotelNameByBookingId(int bookingId) {
        return bookingRepo.getHotelNameByBookingId(bookingId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public List<Booking> searchBookings(String keyword) {
        return bookingRepo.searchByKeyword(keyword);
    }

    // Lấy các booking theo trạng thái refund
    public List<Booking> getBookingsByRefundStatus(String status) {
        return bookingRepo.findByRefundStatus(status);
    }

    // Lấy theo khách sạn (ví dụ dùng trong admin hoặc host dashboard)
    public List<Booking> getBookingsByHotel(int hotelId) {
        return bookingRepo.findByHotelId(hotelId);
    }

    public String getRoomNameByBookingId(int bookingId) {
        return bookingRepo.getRoomNameByBookingId(bookingId);
    }

    public List<BookingRepo.DailyStat> getCheckInStats() {
        return bookingRepo.getCheckInStats();
    }

    public List<BookingRepo.DailyStat> getCheckOutStats() {
        return bookingRepo.getCheckOutStats();
    }

}
