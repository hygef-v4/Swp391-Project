package org.swp391.hotelbookingsystem.controller.user;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.controller.NotificationController;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.BookingUnit;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.NotificationService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingHistoryController {

    @Value("${app.base-url}")
    private String baseUrl;
    private BookingService bookingService;
    private NotificationService notificationService;

    public BookingHistoryController(BookingService bookingService, NotificationService notificationService) {
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @GetMapping("/bookingHistory")
    public String bookingHistory(
            HttpSession session,
            Model model,
            @RequestParam(value = "tab", defaultValue = "upcoming") String tab,
            @RequestParam(value = "pageUpcoming", defaultValue = "1") int pageUpcoming,
            @RequestParam(value = "pageCancelled", defaultValue = "1") int pageCancelled,
            @RequestParam(value = "pageCompleted", defaultValue = "1") int pageCompleted,
            @RequestParam(value = "pageCheckin", defaultValue = "1") int pageCheckin,
            @RequestParam(value = "size", defaultValue = "3") int size
    ) {
        User user = (User) session.getAttribute("user");
        int customerId = user.getId();

        List<Booking> upcomingBookings = bookingService.getUpcomingBookingsPaginated(customerId, pageUpcoming - 1, size);
        List<Booking> cancelledBookings = bookingService.getCancelledBookingsPaginated(customerId, pageCancelled - 1, size);
        List<Booking> completedBookings = bookingService.getCompletedBookingsPaginated(customerId, pageCompleted - 1, size);
        List<Booking> checkinBookings = bookingService.getCheckinBookingsPaginated(customerId, pageCheckin - 1, size);

        int totalPagesUpcoming = bookingService.getTotalPagesUpcoming(customerId, size);
        int totalPagesCancelled = bookingService.getTotalPagesCancelled(customerId, size);
        int totalPagesCompleted = bookingService.getTotalPagesCompleted(customerId, size);
        int totalPagesCheckin = bookingService.getTotalPagesCheckin(customerId, size);

        int totalUpcomingBookings = bookingService.getTotalUpcomingBookings(customerId);
        int totalCancelledBookings = bookingService.getTotalCancelledBookings(customerId);
        int totalCompletedBookings = bookingService.getTotalCompletedBookings(customerId);
        int totalCheckinBookings = bookingService.getTotalCheckinBookings(customerId);

        model.addAttribute("totalUpcomingBookings", totalUpcomingBookings);
        model.addAttribute("totalCancelledBookings", totalCancelledBookings);
        model.addAttribute("totalCompletedBookings", totalCompletedBookings);
        model.addAttribute("totalCheckinBookings", totalCheckinBookings);

        model.addAttribute("tab", tab);

        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("checkinBookings", checkinBookings);

        model.addAttribute("currentPageUpcoming", pageUpcoming);
        model.addAttribute("currentPageCancelled", pageCancelled);
        model.addAttribute("currentPageCompleted", pageCompleted);
        model.addAttribute("currentPageCheckin", pageCheckin);

        model.addAttribute("totalPagesUpcoming", totalPagesUpcoming);
        model.addAttribute("totalPagesCancelled", totalPagesCancelled);
        model.addAttribute("totalPagesCompleted", totalPagesCompleted);
        model.addAttribute("totalPagesCheckin", totalPagesCheckin);

        return "page/bookingHistory";
    }

    @GetMapping("/booking-detail/{id}")
    public String getBookingDetail(
        @PathVariable int id,
        @RequestParam(value = "upcoming", defaultValue = "false") boolean upcoming,
        @RequestParam(value = "cancelled", defaultValue = "false") boolean cancelled,
        @RequestParam(value = "completed", defaultValue = "false") boolean completed,
        @RequestParam(value = "checkin", defaultValue = "false") boolean checkin,
        Model model, HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Booking booking = bookingService.findById(id);
        model.addAttribute("booking", booking);

        model.addAttribute("upcoming", upcoming);
        model.addAttribute("cancelled", cancelled);
        model.addAttribute("completed", completed);
        model.addAttribute("checkin", checkin);

        boolean cancelable = true;
        for (BookingUnit bookingUnit : booking.getBookingUnits()) {
            if(!"approved".equalsIgnoreCase(bookingUnit.getStatus())){
                cancelable = false;
                System.out.println(bookingUnit.getBookingUnitId());
                break;
            }
        }model.addAttribute("cancelable", (cancelable && booking.refundAmount() > 0));
        
        return "page/bookingDetail";
    }

    @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable("id") int bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(booking.getBookingId()));
        if(booking.refundAmount() == booking.getTotalPrice()){
            params.add("trantype", "02");
        }else{
            params.add("trantype", "03");
        }params.add("amount", String.valueOf(booking.refundAmount()));
        params.add("refundRole", "customer");
        params.add("orderInfo", "Hủy đặt phòng " + booking.getHotelName());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        String response = restTemplate.postForObject(baseUrl + "/refund", request, String.class);
        
        if (booking.getCustomerId() == user.getId() && response != null && response.equals("00")) {
            if (booking.getCheckIn().isAfter(LocalDate.now().atStartOfDay())) {
                bookingService.updateBookingStatus(booking, "cancelled");
                notificationService.notifyRefundSuccess(user.getId(), String.valueOf(booking.getBookingId()), booking.refundAmount());
            }
        }

        if(response != null){
            switch(response){
                case "00":
                    redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt phòng thành công");
                    break;
                case "94":
                    redirectAttributes.addFlashAttribute("errorMessage", "Đang xử lý yêu cầu hoàn tiền trước đó");
                    break;
                case "95":
                    redirectAttributes.addFlashAttribute("errorMessage", "VNPAY từ chối xử lý yêu cầu");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không xác định");
                    break;
            }
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không xác định");
        }

        return "redirect:/bookingHistory";
    }

}
