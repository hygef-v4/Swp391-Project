package org.swp391.hotelbookingsystem.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.repository.BookingRepo;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepo bookingRepo;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private BookingUnit testBookingUnit;
    private List<BookingUnit> bookingUnits;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setBookingId(1);
        testBooking.setCustomerId(100);
        testBooking.setHotelId(200);
        testBooking.setCreatedAt(java.time.LocalDateTime.now());

        testBookingUnit = new BookingUnit();
        testBookingUnit.setBookingUnitId(1);
        testBookingUnit.setStatus("pending");
        testBookingUnit.setPrice(100.0); // Set a default price to avoid NullPointerException

        bookingUnits = new ArrayList<>();
        bookingUnits.add(testBookingUnit);
    }

    // Helper method to create BookingUnit with price to avoid NullPointerException
    private BookingUnit createBookingUnitWithPrice(String status, double price) {
        BookingUnit unit = new BookingUnit();
        unit.setStatus(status);
        unit.setPrice(price);
        return unit;
    }

    // Test updateStatus method
    @Test
    void testUpdateStatus_Success() {
        when(bookingRepo.updateStatus(1, "approved")).thenReturn(1);

        bookingService.updateStatus(testBookingUnit, "approved");

        verify(bookingRepo).updateStatus(1, "approved");
    }

    // Test saveBooking method
    @Test
    void testSaveBooking_Success() {
        doNothing().when(bookingRepo).saveBooking(testBooking);

        bookingService.saveBooking(testBooking);

        verify(bookingRepo).saveBooking(testBooking);
    }

    // Test findById method
    @Test
    void testFindById_Success() {
        when(bookingRepo.findById(1)).thenReturn(testBooking);

        Booking result = bookingService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getBookingId());
        verify(bookingRepo).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        when(bookingRepo.findById(999)).thenReturn(null);

        Booking result = bookingService.findById(999);

        assertNull(result);
        verify(bookingRepo).findById(999);
    }

    // Test findBooking method
    @Test
    void testFindBooking_Success() {
        when(bookingRepo.findBookingByBookingUnitId(1)).thenReturn(testBooking);

        Booking result = bookingService.findBooking(1);

        assertNotNull(result);
        assertEquals(testBooking.getBookingId(), result.getBookingId());
        verify(bookingRepo).findBookingByBookingUnitId(1);
    }

    // Test findBookingUnitById method
    @Test
    void testFindBookingUnitById_Success() {
        when(bookingRepo.findBookingUnitById(1)).thenReturn(testBookingUnit);

        BookingUnit result = bookingService.findBookingUnitById(1);

        assertNotNull(result);
        assertEquals(1, result.getBookingUnitId());
        verify(bookingRepo).findBookingUnitById(1);
    }

    // Test countBookingsByHostId method
    @Test
    void testCountBookingsByHostId_Success() {
        when(bookingRepo.countBookingsByHostId(100)).thenReturn(5);

        int result = bookingService.countBookingsByHostId(100);

        assertEquals(5, result);
        verify(bookingRepo).countBookingsByHostId(100);
    }

    @Test
    void testCountBookingsByHostId_ZeroBookings() {
        when(bookingRepo.countBookingsByHostId(999)).thenReturn(0);

        int result = bookingService.countBookingsByHostId(999);

        assertEquals(0, result);
        verify(bookingRepo).countBookingsByHostId(999);
    }

    // Test getAllBookings method
    @Test
    void testGetAllBookings_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findAll()).thenReturn(bookings);

        List<Booking> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findAll();
    }

    @Test
    void testGetAllBookings_EmptyList() {
        when(bookingRepo.findAll()).thenReturn(new ArrayList<>());

        List<Booking> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepo).findAll();
    }

    // Test getTotalBooking method
    @Test
    void testGetTotalBooking_Success() {
        when(bookingRepo.getTotalBookingByStatus("completed")).thenReturn(10);

        int result = bookingService.getTotalBooking("completed");

        assertEquals(10, result);
        verify(bookingRepo).getTotalBookingByStatus("completed");
    }

    // Test getTodayBooking method
    @Test
    void testGetTodayBooking_Success() {
        when(bookingRepo.getTodayBookingByStatus("pending")).thenReturn(3);

        int result = bookingService.getTodayBooking("pending");

        assertEquals(3, result);
        verify(bookingRepo).getTodayBookingByStatus("pending");
    }

    // Test calculateBookingStatus method - Core business logic
    @Test
    void testCalculateBookingStatus_NullList() {
        String result = bookingService.calculateBookingStatus(null);
        assertEquals("unknown", result);
    }

    @Test
    void testCalculateBookingStatus_EmptyList() {
        String result = bookingService.calculateBookingStatus(new ArrayList<>());
        assertEquals("unknown", result);
    }

    @Test
    void testCalculateBookingStatus_AllSameStatus() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("completed");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("completed");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("completed", result);
    }

    @Test
    void testCalculateBookingStatus_PriorityCompleted() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("completed");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("pending");
        BookingUnit unit3 = new BookingUnit();
        unit3.setStatus("approved");

        List<BookingUnit> units = Arrays.asList(unit1, unit2, unit3);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("completed", result);
    }

    @Test
    void testCalculateBookingStatus_PriorityApproved() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("approved");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("pending");
        BookingUnit unit3 = new BookingUnit();
        unit3.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2, unit3);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("approved", result);
    }

    @Test
    void testCalculateBookingStatus_PriorityPending() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("pending");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("pending", result);
    }

    @Test
    void testCalculateBookingStatus_AllCancelled() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("cancelled");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("cancelled", result);
    }

    // Test pagination methods
    @Test
    void testGetUpcomingBookingsPaginated_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findBookingsByStatusAndCustomerPaginated(100, "approved", "future", 0, 10))
            .thenReturn(bookings);

        List<Booking> result = bookingService.getUpcomingBookingsPaginated(100, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findBookingsByStatusAndCustomerPaginated(100, "approved", "future", 0, 10);
    }

    @Test
    void testGetCancelledBookingsPaginated_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findBookingsByStatusAndCustomerPaginated(100, "cancelled_or_rejected", null, 0, 10))
            .thenReturn(bookings);

        List<Booking> result = bookingService.getCancelledBookingsPaginated(100, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findBookingsByStatusAndCustomerPaginated(100, "cancelled_or_rejected", null, 0, 10);
    }

    @Test
    void testGetCompletedBookingsPaginated_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findBookingsByStatusAndCustomerPaginated(100, "completed", "past", 0, 10))
            .thenReturn(bookings);

        List<Booking> result = bookingService.getCompletedBookingsPaginated(100, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findBookingsByStatusAndCustomerPaginated(100, "completed", "past", 0, 10);
    }

    // Test total pages calculation methods
    @Test
    void testGetTotalPagesUpcoming_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(25);

        int result = bookingService.getTotalPagesUpcoming(100, 10);

        assertEquals(3, result); // Math.ceil(25/10) = 3
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "approved", "future");
    }

    @Test
    void testGetTotalPagesUpcoming_ExactDivision() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(20);

        int result = bookingService.getTotalPagesUpcoming(100, 10);

        assertEquals(2, result); // 20/10 = 2
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "approved", "future");
    }

    @Test
    void testGetTotalPagesUpcoming_ZeroCount() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(0);

        int result = bookingService.getTotalPagesUpcoming(100, 10);

        assertEquals(0, result);
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "approved", "future");
    }

    @Test
    void testGetTotalPagesCancelled_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "cancelled_or_rejected", null)).thenReturn(15);

        int result = bookingService.getTotalPagesCancelled(100, 10);

        assertEquals(2, result); // Math.ceil(15/10) = 2
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "cancelled_or_rejected", null);
    }

    @Test
    void testGetTotalPagesCompleted_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "completed", "past")).thenReturn(7);

        int result = bookingService.getTotalPagesCompleted(100, 10);

        assertEquals(1, result); // Math.ceil(7/10) = 1
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "completed", "past");
    }

    // Test revenue and statistics methods
    @Test
    void testGetMonthlyRevenueByHostId_Success() {
        when(bookingRepo.getMonthlyRevenueByHostId(100)).thenReturn(50000.0);

        double result = bookingService.getMonthlyRevenueByHostId(100);

        assertEquals(50000.0, result);
        verify(bookingRepo).getMonthlyRevenueByHostId(100);
    }

    @Test
    void testGetTotalRevenueByHostId_Success() {
        when(bookingRepo.getTotalRevenueByHostId(100)).thenReturn(150000.0);

        Double result = bookingService.getTotalRevenueByHostId(100);

        assertEquals(150000.0, result);
        verify(bookingRepo).getTotalRevenueByHostId(100);
    }

    @Test
    void testGetTotalRevenueByHostId_NullRevenue() {
        when(bookingRepo.getTotalRevenueByHostId(999)).thenReturn(null);

        Double result = bookingService.getTotalRevenueByHostId(999);

        assertNull(result);
        verify(bookingRepo).getTotalRevenueByHostId(999);
    }

    @Test
    void testGetBookingStatsByHostId_Success() {
        List<Map<String, Object>> stats = Arrays.asList(
            Map.of("month", "January", "bookings", 10),
            Map.of("month", "February", "bookings", 15)
        );
        when(bookingRepo.getBookingStatsByHostId(100, "monthly")).thenReturn(stats);

        List<Map<String, Object>> result = bookingService.getBookingStatsByHostId(100, "monthly");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingRepo).getBookingStatsByHostId(100, "monthly");
    }

    // Test count methods
    @Test
    void testCountTotalBookingsByHostId_Success() {
        when(bookingRepo.countBookingsByHostId(100)).thenReturn(25);

        int result = bookingService.countTotalBookingsByHostId(100);

        assertEquals(25, result);
        verify(bookingRepo).countBookingsByHostId(100);
    }

    @Test
    void testCountCompletedBookingsByHostId_Success() {
        when(bookingRepo.countBookingsByHostIdAndStatus(100, "completed")).thenReturn(15);

        int result = bookingService.countCompletedBookingsByHostId(100);

        assertEquals(15, result);
        verify(bookingRepo).countBookingsByHostIdAndStatus(100, "completed");
    }

    // Test check-in/check-out methods
    @Test
    void testGetTodayCheckIn_Success() {
        when(bookingRepo.getTodayCheckIn()).thenReturn(5);

        int result = bookingService.getTodayCheckIn();

        assertEquals(5, result);
        verify(bookingRepo).getTodayCheckIn();
    }

    @Test
    void testGetTodayCheckOut_Success() {
        when(bookingRepo.getTodayCheckOut()).thenReturn(3);

        int result = bookingService.getTodayCheckOut();

        assertEquals(3, result);
        verify(bookingRepo).getTodayCheckOut();
    }

    @Test
    void testGetFutureCheckIn_Success() {
        when(bookingRepo.getFutureCheckIn()).thenReturn(12);

        int result = bookingService.getFutureCheckIn();

        assertEquals(12, result);
        verify(bookingRepo).getFutureCheckIn();
    }

    @Test
    void testGetFutureCheckOut_Success() {
        when(bookingRepo.getFutureCheckOut()).thenReturn(8);

        int result = bookingService.getFutureCheckOut();

        assertEquals(8, result);
        verify(bookingRepo).getFutureCheckOut();
    }

    // Test updateBookingUnitStatus method
    @Test
    void testUpdateBookingUnitStatus_Success() {
        when(bookingRepo.updateStatus(1, "cancelled")).thenReturn(1);

        bookingService.updateBookingUnitStatus(1, "cancelled");

        verify(bookingRepo).updateStatus(1, "cancelled");
    }

    // Test getBookingsByHotelId method
    @Test
    void testGetBookingsByHotelId_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findByHotelId(200)).thenReturn(bookings);

        List<Booking> result = bookingService.getBookingsByHotelId(200);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findByHotelId(200);
    }

    @Test
    void testGetBookingsByHotelId_EmptyResult() {
        when(bookingRepo.findByHotelId(999)).thenReturn(new ArrayList<>());

        List<Booking> result = bookingService.getBookingsByHotelId(999);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepo).findByHotelId(999);
    }

    // Test total count methods
    @Test
    void testGetTotalCompletedBookings_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "completed", "past")).thenReturn(20);

        int result = bookingService.getTotalCompletedBookings(100);

        assertEquals(20, result);
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "completed", "past");
    }

    @Test
    void testGetTotalUpcomingBookings_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(5);

        int result = bookingService.getTotalUpcomingBookings(100);

        assertEquals(5, result);
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "approved", "future");
    }

    @Test
    void testGetTotalCancelledBookings_Success() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "cancelled_or_rejected", null)).thenReturn(3);

        int result = bookingService.getTotalCancelledBookings(100);

        assertEquals(3, result);
        verify(bookingRepo).countBookingsByStatusAndCustomer(100, "cancelled_or_rejected", null);
    }

    // Test edge cases for calculateBookingStatus
    @Test
    void testCalculateBookingStatus_SingleUnit() {
        BookingUnit unit = new BookingUnit();
        unit.setStatus("pending");

        List<BookingUnit> units = Arrays.asList(unit);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("pending", result);
    }

    @Test
    void testCalculateBookingStatus_MixedStatuses() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("cancelled");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("pending");
        BookingUnit unit3 = new BookingUnit();
        unit3.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2, unit3);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("pending", result);
    }

    // Test countBookingsByRoomId method
    @Test
    void testCountBookingsByRoomId_Success() {
        when(bookingRepo.countBookingsByRoomId(300)).thenReturn(8);

        int result = bookingService.countBookingsByRoomId(300);

        assertEquals(8, result);
        verify(bookingRepo).countBookingsByRoomId(300);
    }

    // Test countBookingsByHotelId method
    @Test
    void testCountBookingsByHotelId_Success() {
        when(bookingRepo.countBookingsByHotelId(200)).thenReturn(15);

        int result = bookingService.countBookingsByHotelId(200);

        assertEquals(15, result);
        verify(bookingRepo).countBookingsByHotelId(200);
    }

    // Test getBookingsByHostId method
    @Test
    void testGetBookingsByHostId_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.findBookingsByHostId(100)).thenReturn(bookings);

        List<Booking> result = bookingService.getBookingsByHostId(100);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).findBookingsByHostId(100);
    }

    // Test getBookingsByStatusAndSearchPaginated method - Complex business logic
    @Test
    void testGetBookingsByStatusAndSearchPaginated_WithStatus() {
        Booking booking1 = new Booking();
        booking1.setBookingId(1);
        booking1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

        Booking booking2 = new Booking();
        booking2.setBookingId(2);
        booking2.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));

        List<Booking> bookings = Arrays.asList(booking1, booking2);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));
        when(bookingRepo.findBookingUnitsByBookingId(2))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 120.0)));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("pending", "test", 0, 10);

        assertNotNull(result);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
        verify(bookingRepo).findBookingUnitsByBookingId(1);
        verify(bookingRepo).findBookingUnitsByBookingId(2);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_NullStatus() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated(null, "test", 0, 10);

        assertNotNull(result);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_BlankStatus() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("", "test", 0, 10);

        assertNotNull(result);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_FromIndexExceedsSize() {
        List<Booking> bookings = Arrays.asList();

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("pending", "test", 5, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_SublistScenario() {
        // Create multiple bookings to test sublist functionality
        Booking booking1 = new Booking();
        booking1.setBookingId(1);
        booking1.setCreatedAt(java.time.LocalDateTime.now().minusDays(3));

        Booking booking2 = new Booking();
        booking2.setBookingId(2);
        booking2.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));

        Booking booking3 = new Booking();
        booking3.setBookingId(3);
        booking3.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

        List<Booking> bookings = Arrays.asList(booking1, booking2, booking3);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        // Request page 0 with size 2, should return first 2 items
        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated(null, "test", 0, 2);

        assertNotNull(result);
        assertTrue(result.size() <= 2);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    // Test getTotalPagesByStatusAndSearch method
    @Test
    void testGetTotalPagesByStatusAndSearch_WithStatus() {
        Booking booking1 = new Booking();
        booking1.setBookingId(1);
        booking1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

        Booking booking2 = new Booking();
        booking2.setBookingId(2);
        booking2.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));

        List<Booking> bookings = Arrays.asList(booking1, booking2);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        int result = bookingService.getTotalPagesByStatusAndSearch("pending", "test", 10);

        assertTrue(result >= 0);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_NullStatus() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        int result = bookingService.getTotalPagesByStatusAndSearch(null, "test", 10);

        assertTrue(result >= 0);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_BlankStatus() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(createBookingUnitWithPrice("pending", 100.0)));

        int result = bookingService.getTotalPagesByStatusAndSearch("", "test", 10);

        assertTrue(result >= 0);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    // Test getBookingsByHotelIdPaginated method
    @Test
    void testGetBookingsByHotelIdPaginated_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.getBookingsByHotelIdPaginated(200, 0, 10)).thenReturn(bookings);

        List<Booking> result = bookingService.getBookingsByHotelIdPaginated(200, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepo).getBookingsByHotelIdPaginated(200, 0, 10);
    }

    @Test
    void testGetBookingsByHotelIdPaginated_EmptyResult() {
        when(bookingRepo.getBookingsByHotelIdPaginated(999, 0, 10)).thenReturn(new ArrayList<>());

        List<Booking> result = bookingService.getBookingsByHotelIdPaginated(999, 0, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepo).getBookingsByHotelIdPaginated(999, 0, 10);
    }

    @Test
    void testGetBookingsByHotelIdPaginated_OffsetCalculation() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepo.getBookingsByHotelIdPaginated(200, 20, 10)).thenReturn(bookings);

        // Test page 2 with size 10, offset should be 2 * 10 = 20
        List<Booking> result = bookingService.getBookingsByHotelIdPaginated(200, 2, 10);

        assertNotNull(result);
        verify(bookingRepo).getBookingsByHotelIdPaginated(200, 20, 10);
    }

    // Test additional edge cases for calculateBookingStatus
    @Test
    void testCalculateBookingStatus_UnknownStatus() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("unknown");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("unknown");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("unknown", result);
    }

    @Test
    void testCalculateBookingStatus_AllPossibleStatuses() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("completed");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("approved");
        BookingUnit unit3 = new BookingUnit();
        unit3.setStatus("pending");
        BookingUnit unit4 = new BookingUnit();
        unit4.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2, unit3, unit4);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("completed", result); // completed has highest priority
    }

    @Test
    void testCalculateBookingStatus_NoCompletedButHasApproved() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("approved");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("pending");
        BookingUnit unit3 = new BookingUnit();
        unit3.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2, unit3);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("approved", result); // approved has second highest priority
    }

    @Test
    void testCalculateBookingStatus_OnlyPendingAndCancelled() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("pending");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("pending", result); // pending has third highest priority
    }

    @Test
    void testCalculateBookingStatus_OnlyCancelled() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("cancelled");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("cancelled");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("cancelled", result);
    }

    @Test
    void testCalculateBookingStatus_OnlyOtherStatuses() {
        BookingUnit unit1 = new BookingUnit();
        unit1.setStatus("rejected");
        BookingUnit unit2 = new BookingUnit();
        unit2.setStatus("expired");

        List<BookingUnit> units = Arrays.asList(unit1, unit2);

        String result = bookingService.calculateBookingStatus(units);
        assertEquals("cancelled", result); // falls back to cancelled when no priority status found
    }

    // Test edge cases for pagination methods with zero counts
    @Test
    void testGetTotalPagesUpcoming_ZeroSizeParameter() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(10);

        // Math.ceil with division by zero returns positive infinity, cast to int gives Integer.MAX_VALUE
        int result = bookingService.getTotalPagesUpcoming(100, 0);
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    void testGetTotalPagesCancelled_ZeroSizeParameter() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "cancelled_or_rejected", null)).thenReturn(5);

        // Math.ceil with division by zero returns positive infinity, cast to int gives Integer.MAX_VALUE
        int result = bookingService.getTotalPagesCancelled(100, 0);
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    void testGetTotalPagesCompleted_ZeroSizeParameter() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "completed", "past")).thenReturn(7);

        // Math.ceil with division by zero returns positive infinity, cast to int gives Integer.MAX_VALUE
        int result = bookingService.getTotalPagesCompleted(100, 0);
        assertEquals(Integer.MAX_VALUE, result);
    }

    // Test edge case for Math.ceil calculations
    @Test
    void testGetTotalPagesUpcoming_FractionalResult() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(11);

        int result = bookingService.getTotalPagesUpcoming(100, 10);

        assertEquals(2, result); // Math.ceil(11/10.0) = 2
    }

    @Test
    void testGetTotalPagesUpcoming_LessThanPageSize() {
        when(bookingRepo.countBookingsByStatusAndCustomer(100, "approved", "future")).thenReturn(5);

        int result = bookingService.getTotalPagesUpcoming(100, 10);

        assertEquals(1, result); // Math.ceil(5/10.0) = 1
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_EmptyResult() {
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(new ArrayList<>());

        int result = bookingService.getTotalPagesByStatusAndSearch("pending", "test", 10);

        assertEquals(0, result);
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    // Test to cover missing branches in filter conditions
    @Test
    void testGetBookingsByStatusAndSearchPaginated_StatusNotEqualsBookingStatus() {
        // Test the case where status != null AND !status.isBlank() AND !status.equals(b.getStatus())
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        // Mock the booking to have a different status than what we're filtering for
        BookingUnit unit = new BookingUnit();
        unit.setStatus("approved"); // booking will have "approved" status
        unit.setPrice(150.0); // Set price to avoid NullPointerException

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(unit));

        // Search for "pending" status, but booking has "approved" status - should be filtered out
        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("pending", "test", 0, 10);

        assertNotNull(result);
        assertEquals(0, result.size()); // Should be empty since status doesn't match
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_StatusNotEqualsBookingStatus() {
        // Test the same branch logic for getTotalPagesByStatusAndSearch
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        List<Booking> bookings = Arrays.asList(booking);

        BookingUnit unit = new BookingUnit();
        unit.setStatus("approved"); // booking will have "approved" status
        unit.setPrice(150.0); // Set price to avoid NullPointerException

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE))
            .thenReturn(bookings);
        when(bookingRepo.findBookingUnitsByBookingId(1))
            .thenReturn(Arrays.asList(unit));

        // Search for "pending" status, but booking has "approved" status - should be filtered out
        int result = bookingService.getTotalPagesByStatusAndSearch("pending", "test", 10);

        assertEquals(0, result); // Should be 0 since no bookings match the status
        verify(bookingRepo).findBookingsByStatusAndKeywordPaginated(null, "test", 0, Integer.MAX_VALUE);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_StatusNull() {
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated(null, "key", 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_StatusBlank() {
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("   ", "key", 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_StatusNull() {
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        int result = bookingService.getTotalPagesByStatusAndSearch(null, "key", 10);
        assertEquals(1, result);
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_StatusBlank() {
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        int result = bookingService.getTotalPagesByStatusAndSearch("", "key", 10);
        assertEquals(1, result);
    }

    @Test
    void testGetBookingsByStatusAndSearchPaginated_NoMatchStatus() {
        testBooking.setStatus("approved"); // Repo will return "approved"

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("rejected", "key", 0, 10);
        assertEquals(0, result.size());
    }

    @Test
    void testGetTotalPagesByStatusAndSearch_NoMatchStatus() {
        testBooking.setStatus("completed");

        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));

        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        int result = bookingService.getTotalPagesByStatusAndSearch("pending", "key", 10);
        assertEquals(0, result); // since no match
    }

    @Test
    void testGBSASP_Filter_WhenStatusIsNull() {
        // SCENARIO 1: Covers the `status == null` branch. The filter should pass all items.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated(null, "key", 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void testGBSASP_Filter_WhenStatusIsBlank() {
        // SCENARIO 2: Covers the `status.isBlank()` branch. The filter should pass all items.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("   ", "key", 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void testGBSASP_Filter_WhenStatusMatches() {
        // SCENARIO 3: Covers `status.equals(b.getStatus())` being true. The filter should pass the item.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit)); // Determines status as "pending"

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("pending", "key", 0, 10);

        assertEquals(1, result.size());
        assertEquals("pending", result.get(0).getStatus());
    }

    @Test
    void testGBSASP_Filter_WhenStatusDoesNotMatch() {
        // SCENARIO 4: Covers `status.equals(b.getStatus())` being false. The filter should reject the item.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit)); // Determines status as "pending"

        List<Booking> result = bookingService.getBookingsByStatusAndSearchPaginated("approved", "key", 0, 10);

        assertEquals(0, result.size());
    }

    // --- Tests for getTotalPagesByStatusAndSearch ---

    @Test
    void testGTPSAS_Filter_WhenStatusIsNull() {
        // SCENARIO 1: Covers the `status == null` branch.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        int result = bookingService.getTotalPagesByStatusAndSearch(null, "key", 10);

        assertEquals(1, result);
    }

    @Test
    void testGTPSAS_Filter_WhenStatusIsBlank() {
        // SCENARIO 2: Covers the `status.isBlank()` branch.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit));

        int result = bookingService.getTotalPagesByStatusAndSearch("", "key", 10);

        assertEquals(1, result);
    }

    @Test
    void testGTPSAS_Filter_WhenStatusMatches() {
        // SCENARIO 3: Covers `status.equals(b.getStatus())` being true.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit)); // Determines status as "pending"

        int result = bookingService.getTotalPagesByStatusAndSearch("pending", "key", 10);

        assertEquals(1, result);
    }

    @Test
    void testGTPSAS_Filter_WhenStatusDoesNotMatch() {
        // SCENARIO 4: Covers `status.equals(b.getStatus())` being false.
        when(bookingRepo.findBookingsByStatusAndKeywordPaginated(null, "key", 0, Integer.MAX_VALUE))
                .thenReturn(List.of(testBooking));
        when(bookingRepo.findBookingUnitsByBookingId(anyInt()))
                .thenReturn(List.of(testBookingUnit)); // Determines status as "pending"

        int result = bookingService.getTotalPagesByStatusAndSearch("approved", "key", 10);

        assertEquals(0, result);
    }

}