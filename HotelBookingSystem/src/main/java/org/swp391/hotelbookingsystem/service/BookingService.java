package org.swp391.hotelbookingsystem.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

    public int bookedRoom(int roomId, LocalDateTime checkIn, LocalDateTime checkOut){
        return bookingRepo.bookedRoom(roomId, checkIn, checkOut);
    }

    public boolean checkQuantity(Booking booking){
        for(BookingUnit bookingUnit : booking.getBookingUnits()){
            if(bookingRepo.bookedRoom(bookingUnit.getRoomId(), booking.getCheckIn(), booking.getCheckOut()) <= 0){
                return false;
            }
        }return true;
    }

    public void updateBookingStatus(Booking booking, String status){
        for(BookingUnit unit : booking.getBookingUnits()){
            bookingRepo.updateStatus(unit.getBookingUnitId(), status);
        }
        booking.setStatus(status);
    }

    /**
     * Reject only approved booking units, leave other statuses unchanged
     * @param booking The booking to process
     * @return Number of booking units that were actually rejected
     */
    public int rejectApprovedBookingUnits(Booking booking) {
        int rejectedCount = 0;
        for(BookingUnit unit : booking.getBookingUnits()){
            if("approved".equals(unit.getStatus())) {
                bookingRepo.updateStatus(unit.getBookingUnitId(), "rejected");
                rejectedCount++;
            }
        }
        // Update booking status based on remaining units
        booking.setBookingUnits(bookingRepo.findBookingUnitsByBookingId(booking.getBookingId()));
        booking.setStatus(booking.determineStatus());
        return rejectedCount;
    }

    public void updateStatus(BookingUnit bookingUnit, String status){
        bookingRepo.updateStatus(bookingUnit.getBookingUnitId(), status);
    }

    public int pendingBooking(Booking booking){
        return bookingRepo.pendingBooking(booking);
    }

    public int remainPendingTime(int bookingId){
        return bookingRepo.remainPendingTime(bookingId);
    }

    public int approveBooking(int id, String orderCode, String transactionNo, String payDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createdAt = LocalDateTime.parse(payDate, formatter);
        
        bookingRepo.setTransactionDetails(id, orderCode, transactionNo, createdAt);
        return bookingRepo.approveBookingUnit(id);
    }

    public void deletePendingBooking(int id, int userId){
        if(bookingRepo.isPending(id, userId)){
            bookingRepo.deletePendingBooking(id);
        }
    }

    public int saveBooking(Booking booking){
        return bookingRepo.saveBooking(booking);
    }

    public Booking findById(int id){
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

    public List<Booking> getBookingByCustomerId(int customerId){
        return bookingRepo.findBookingsByCustomerId(customerId);
    }

    public int countTotalBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostId(hostId);
    }

    public int countCompletedBookingsByHostId(int hostId) {
        return bookingRepo.countBookingsByHostIdAndStatus(hostId, "completed");
    }

    public int countPaidBookingsByHostId(int hostId) {
        return bookingRepo.countPaidBookingsByHostId(hostId);
    }

    public int countActiveBookingsByHotelId(int hotelId) {
        return bookingRepo.countActiveBookingsByHotelId(hotelId);
    }

    public int countApprovedBookingsByHotelId(int hotelId) {
        return bookingRepo.countApprovedBookingsByHotelId(hotelId);
    }

    public int countCheckedInBookingsByHotelId(int hotelId) {
        return bookingRepo.countCheckedInBookingsByHotelId(hotelId);
    }

    public List<Booking> findActiveBookingsByHotelId(int hotelId) {
        return bookingRepo.findActiveBookingsByHotelId(hotelId);
    }

    public int rejectAllActiveBookingsByHotelId(int hotelId) {
        return bookingRepo.rejectAllActiveBookingsByHotelId(hotelId);
    }

    public int countActiveBookingsByRoomId(int roomId) {
        return bookingRepo.countActiveBookingsByRoomId(roomId);
    }

    public int countApprovedBookingsByRoomId(int roomId) {
        return bookingRepo.countApprovedBookingsByRoomId(roomId);
    }

    public int countAllApprovedBookingUnitsInAffectedBookings(int roomId) {
        return bookingRepo.countAllApprovedBookingUnitsInAffectedBookings(roomId);
    }

    public int countCheckedInBookingsByRoomId(int roomId) {
        return bookingRepo.countCheckedInBookingsByRoomId(roomId);
    }

    public List<Booking> findActiveBookingsByRoomId(int roomId) {
        return bookingRepo.findActiveBookingsByRoomId(roomId);
    }

    public int rejectAllActiveBookingsByRoomId(int roomId) {
        return bookingRepo.rejectAllActiveBookingsByRoomId(roomId);
    }

    /**
     * Get the original total price from the booking table for refund calculation
     * @param bookingId The booking ID
     * @return The original total price paid by customer
     */
    public Double getOriginalTotalPrice(int bookingId) {
        return bookingRepo.getOriginalTotalPrice(bookingId);
    }

    public double getMonthlyRevenueByHostId(int hostId) {
        return bookingRepo.getMonthlyRevenueByHostId(hostId);
    }

    public Double getTotalRevenueByHostId(int hostId) {
        return bookingRepo.getTotalRevenueByHostId(hostId);
    }

    public List<Map<String, Object>> getBookingStatsByHostId(int hostId, String period) {
        List<Map<String, Object>> rawStats = bookingRepo.getBookingStatsByHostId(hostId, period);
        if (!"6months".equals(period)) {
            return rawStats;
        }
        // Build a map for quick lookup
        Map<String, Integer> monthToCount = new HashMap<>();
        for (Map<String, Object> stat : rawStats) {
            String category = String.valueOf(stat.get("category"));
            Integer count = stat.get("count") == null ? 0 : Integer.parseInt(stat.get("count").toString());
            monthToCount.put(category, count);
        }
        // Generate last 6 months (including current month)
        List<Map<String, Object>> result = new ArrayList<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            String key = ym.format(formatter);
            Map<String, Object> item = new HashMap<>();
            item.put("category", key);
            item.put("count", monthToCount.getOrDefault(key, 0));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getRevenueStatsByHostId(int hostId, String period) {
        List<Map<String, Object>> rawStats = bookingRepo.getRevenueStatsByHostId(hostId, period);
        if (!"6months".equals(period)) {
            return rawStats;
        }
        // Build a map for quick lookup
        Map<String, Double> monthToRevenue = new HashMap<>();
        for (Map<String, Object> stat : rawStats) {
            String category = String.valueOf(stat.get("category"));
            Double revenue = stat.get("revenue") == null ? 0.0 : Double.parseDouble(stat.get("revenue").toString());
            monthToRevenue.put(category, revenue);
        }
        // Generate last 6 months (including current month)
        List<Map<String, Object>> result = new ArrayList<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            String key = ym.format(formatter);
            Map<String, Object> item = new HashMap<>();
            item.put("category", key);
            item.put("revenue", monthToRevenue.getOrDefault(key, 0.0));
            result.add(item);
        }
        return result;
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

        // Prioritized status logic
        if (statuses.contains("completed")) {
            return "completed";
        } else if (statuses.contains("check_in")) {
            return "check_in";
        } else if (statuses.contains("approved")) {
            return "approved";
        } else if (statuses.contains("pending")) {
            return "pending";
        } else if (statuses.contains("rejected")) {
            return "rejected";
        } else {
            return "cancelled"; // fallback
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
            booking.calculateNumberOfNights();
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

    public List<Booking> getBookingsByHotelIdPaginated(int hotelId, int page, int size, String search, String sort) {
        int offset = page * size;
        return bookingRepo.getBookingsByHotelIdPaginated(hotelId, offset, size, search, sort);
    }

    public List<Booking> getBookingsByHotelIdOrderByDate(int hotelId) {
        return bookingRepo.getBookingsByHotelIdOrderByDate(hotelId);
    }

    public int countBookingsByHotelId(int hotelId, String search) {
        return bookingRepo.countBookingsByHotelId(hotelId, search);
    }

    public int countBookingsByHotelId(int hotelId) {
        return bookingRepo.countBookingsByHotelId(hotelId);
    }
}
