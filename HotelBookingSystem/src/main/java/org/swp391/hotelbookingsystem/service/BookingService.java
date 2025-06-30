package org.swp391.hotelbookingsystem.service;

import java.util.Comparator;
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

    public void updateStatus(BookingUnit bookingUnit, String status) {
        bookingRepo.updateStatus(bookingUnit.getBookingUnitId(), status);
    }

    public int pendingBooking(Booking booking){
        return bookingRepo.pendingBooking(booking);
    }

    public void approveBooking(int id){
        bookingRepo.approveBookingUnit(id);
    }

    public void deletePendingBooking(int id, int userId){
        if(bookingRepo.isPending(id, userId)){
            bookingRepo.deletePendingBooking(id);
        }
    }

    public int saveBooking(Booking booking){
        return bookingRepo.saveBooking(booking);
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

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
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

    public List<Booking> getBookingsByHostId(int hostId) {
        return bookingRepo.findBookingsByHostId(hostId);
    }

    public int countTotalBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostId(hostId);
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

    public void updateBookingUnitStatus(int bookingUnitId, String status) {
        bookingRepo.updateStatus(bookingUnitId, status);
    }

    public List<Booking> getBookingsByHotelId(int hotelId) {
        return bookingRepo.findByHotelId(hotelId);
    }

    public List<Booking> getUpcomingBookingsPaginated(int customerId, int page, int size) {
        return bookingRepo.findBookingsByStatusAndCustomerPaginated(customerId, "approved", "future", page, size);
    }

    public List<Booking> getCancelledBookingsPaginated(int customerId, int page, int size) {
        return bookingRepo.findBookingsByStatusAndCustomerPaginated(customerId, "cancelled_or_rejected", null, page, size);
    }

    public List<Booking> getCompletedBookingsPaginated(int customerId, int page, int size) {
        return bookingRepo.findBookingsByStatusAndCustomerPaginated(customerId, "completed", "past", page, size);
    }

    public List<Booking> getCheckinBookingsPaginated(int customerId, int page, int size) {
        return bookingRepo.findBookingsByStatusAndCustomerPaginated(customerId, "check_in", null, page, size);
    }

    public int getTotalPagesUpcoming(int customerId, int size) {
        int count = bookingRepo.countBookingsByStatusAndCustomer(customerId, "approved", "future");
        return (int) Math.ceil((double) count / size);
    }
    public int getTotalPagesCheckin(int customerId, int size) {
        int count = bookingRepo.countBookingsByStatusAndCustomer(customerId, "check_in", null);
        return (int) Math.ceil((double) count / size);
    }

    public int getTotalPagesCancelled(int customerId, int size) {
        int count = bookingRepo.countBookingsByStatusAndCustomer(customerId, "cancelled_or_rejected", null);
        return (int) Math.ceil((double) count / size);
    }

    public int getTotalPagesCompleted(int customerId, int size) {
        int count = bookingRepo.countBookingsByStatusAndCustomer(customerId, "completed", "past");
        return (int) Math.ceil((double) count / size);
    }

    public int getTotalCompletedBookings(int customerId) {
        return bookingRepo.countBookingsByStatusAndCustomer(customerId, "completed", "past");
    }

    public int getTotalCheckinBookings(int customerId) {
        return bookingRepo.countBookingsByStatusAndCustomer(customerId, "check_in", null);
    }

    public int getTotalUpcomingBookings(int customerId) {
        return bookingRepo.countBookingsByStatusAndCustomer(customerId, "approved", "future");
    }
    public int getTotalCancelledBookings(int customerId) {
        return bookingRepo.countBookingsByStatusAndCustomer(customerId, "cancelled_or_rejected", null);
    }

    public List<Booking> getBookingsByStatusAndSearchPaginated(String status, String keyword, int page, int size) {
        List<Booking> all = bookingRepo.findBookingsByStatusAndKeywordPaginated(null, keyword, 0, Integer.MAX_VALUE); // lấy hết

        for (Booking booking : all) {
            booking.setBookingUnits(bookingRepo.findBookingUnitsByBookingId(booking.getBookingId()));
            booking.setStatus(booking.determineStatus());
            booking.setTotalPrice(booking.calculateTotalPrice());
        }

        List<Booking> filtered = all.stream()
                .filter(b -> status == null || status.isBlank() || status.equals(b.getStatus()))
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .toList();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());

        return fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex);
    }

    public int getTotalPagesByStatusAndSearch(String status, String keyword, int size) {
        List<Booking> all = bookingRepo.findBookingsByStatusAndKeywordPaginated(null, keyword, 0, Integer.MAX_VALUE);

        for (Booking booking : all) {
            booking.setBookingUnits(bookingRepo.findBookingUnitsByBookingId(booking.getBookingId()));
            booking.setStatus(booking.determineStatus());
        }

        long count = all.stream()
                .filter(b -> status == null || status.isBlank() || status.equals(b.getStatus()))
                .count();

        return (int) Math.ceil((double) count / size);
    }

    public int countBookingsByRoomId(int roomId) {
        return bookingRepo.countBookingsByRoomId(roomId);
    }

    public List<Booking> getBookingsByHotelIdPaginated(int hotelId, int page, int size) {
        int offset = page * size;
        return bookingRepo.getBookingsByHotelIdPaginated(hotelId, offset, size);
    }

    public List<Booking> getBookingsByHotelIdOrderByDate(int hotelId) {
        return bookingRepo.getBookingsByHotelIdOrderByDate(hotelId);
    }

    public int countBookingsByHotelId(int hotelId) {
        return bookingRepo.countBookingsByHotelId(hotelId);
    }
}
