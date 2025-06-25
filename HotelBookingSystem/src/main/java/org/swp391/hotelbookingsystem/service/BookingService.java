package org.swp391.hotelbookingsystem.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.repository.BookingRepo;

@Service
public class BookingService {

    private final BookingRepo bookingRepo;

    public BookingService(BookingRepo bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    // Hủy đơn đặt phòng
    public void cancelBooking(int id) {
        bookingRepo.updateStatus(id, "cancelled");
    }

    // Đặt lại trạng thái
    public void updateStatus(BookingUnit bookingUnit, String status) {
        bookingRepo.updateStatus(bookingUnit.getBookingUnitId(), status);
    }

    public int saveBooking(Booking booking){
        return bookingRepo.saveBooking(booking);
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

    public Booking findBooking(int id) {
        return bookingRepo.findBookingByBookingUnitId(id);
    }

    public BookingUnit findBookingUnitById(int id) {
        return bookingRepo.findBookingUnitById(id);
    }

    public int countBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostId(hostId);
    }

    // Cập nhật hoàn tiền
    public void updateRefund(int bookingId, Double amount, String status) {
        bookingRepo.updateRefund(bookingId, amount, status);
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

    public List<BookingRepo.DailyStat> getCheckInStats() {
        return bookingRepo.getCheckInStats();
    }

    public List<BookingRepo.DailyStat> getCheckOutStats() {
        return bookingRepo.getCheckOutStats();
    }

    public int getTotalBooking(String status) {
        return bookingRepo.getTotalBookingByStatus(status);
    }

    public int getTodayBooking(String status) {
        return bookingRepo.getTodayBookingByStatus(status);
    }

    public int getTodayCheckIn() {
        return bookingRepo.getTodayCheckIn();
    }

    public int getTodayCheckOut() {
        return bookingRepo.getTodayCheckOut();
    }

    public int getFutureCheckIn() {
        return bookingRepo.getFutureCheckIn();
    }

    public int getFutureCheckOut() {
        return bookingRepo.getFutureCheckOut();
    }

    public List<Booking> getBookingsPaginated(int page, int size) {
        return bookingRepo.findAllPaginated(page, size);
    }

    public int getTotalPages(int size) {
        int total = bookingRepo.countAllBookings();
        return (int) Math.ceil((double) total / size);
    }

    public int getTotalItems() {
        return bookingRepo.countAllBookings();
    }

    public List<Booking> getBookingsByStatusPaginated(String status, int page, int size) {
        return bookingRepo.findBookingsByStatusPaginated(status, page, size);
    }

    public int getTotalPagesByStatus(String status, int size) {
        int count = bookingRepo.countBookingsByStatus(status);
        return (int) Math.ceil((double) count / size);
    }

    public List<Booking> getBookingsByStatusAndSearchPaginated(String status, String keyword, int page, int size) {
        return bookingRepo.findBookingsByStatusAndKeywordPaginated(status, keyword, page, size);
    }

    public int getTotalPagesByStatusAndSearch(String status, String keyword, int size) {
        int total = bookingRepo.countBookingsByStatusAndKeyword(status, keyword);
        return (int) Math.ceil((double) total / size);
    }

    public List<Booking> getBookingsByHostId(int hostId) {
        return bookingRepo.findBookingsByHostId(hostId);
    }

    public int countTotalBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostId(hostId);
    }

    public int countPendingBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostIdAndStatus(hostId, "approved");
    }

    public int countCompletedBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostIdAndStatus(hostId, "completed");
    }

    public double getMonthlyRevenueByHostId(int hostId) {
        return bookingRepo.getMonthlyRevenueByHostId(hostId);
    }

    public Double getTotalRevenueByHostId(int hostId) {
        return bookingRepo.getTotalRevenueByHostId(hostId);
    }

    public List<Map<String, Object>> getBookingStatsByHostId(int hostId, String period) {
        return bookingRepo.getBookingStatsByHostId(hostId, period);
    }

    // Calculate overall booking status based on booking units
    public String calculateBookingStatus(List<BookingUnit> bookingUnits) {
        if (bookingUnits == null || bookingUnits.isEmpty()) {
            return "unknown";
        }

        // Get all unique statuses
        List<String> statuses = bookingUnits.stream()
                .map(BookingUnit::getStatus)
                .distinct()
                .toList();

        // If all units have the same status
        if (statuses.size() == 1) {
            return statuses.get(0);
        }

        // If multiple statuses, prioritize in order: completed > approved > pending > cancelled
        if (statuses.contains("completed")) {
            return "completed";
        } else if (statuses.contains("approved")) {
            return "approved";
        } else if (statuses.contains("pending")) {
            return "pending";
        } else {
            return "cancelled";
        }
    }

    // Update booking unit status
    public void updateBookingUnitStatus(int bookingUnitId, String status) {
        bookingRepo.updateStatus(bookingUnitId, status);
    }

}
