package org.swp391.hotelbookingsystem.controller.host;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.Customer;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HostCustomerController {

    private final BookingService bookingService;
    private final UserService userService;

    public HostCustomerController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/host-customers")
    public String showHostCustomers(HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            log.warn("Unauthorized access to host customers page by user: {}", 
                host != null ? host.getId() : "null");
            return "redirect:/login";
        }

        try {
            log.info("Fetching customers for host: {}", host.getId());
            
            // Get all bookings for this host
            List<Booking> allBookings = bookingService.getBookingsByHostId(host.getId());
            log.debug("Found {} bookings for host {}", allBookings.size(), host.getId());

            // Calculate overall status for each booking
            for (Booking booking : allBookings) {
                String overallStatus = bookingService.calculateBookingStatus(booking.getBookingUnits());
                booking.setStatus(overallStatus);
                double sum = booking.getBookingUnits().stream()
                        .filter(u -> u.getPrice() != null)
                        .mapToDouble(u -> u.getPrice() * (u.getQuantity() == 0 ? 1 : u.getQuantity()))
                        .sum();
                booking.setTotalPrice(sum);
            }

            // Group bookings by customer
            Map<Integer, List<Booking>> customerBookings = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getCustomerId));

            // Create customer summary data
            List<Customer> customers = customerBookings.entrySet().stream()
                    .map(entry -> {
                        int customerId = entry.getKey();
                        List<Booking> bookings = entry.getValue();
                        
                        try {
                            // Get actual customer info from User model
                            User customerUser = userService.findUserById(customerId);
                            if (customerUser == null) {
                                log.warn("Customer not found with ID: {}", customerId);
                                return null; // Skip if customer not found
                            }
                            
                            // Calculate stats
                            long totalBookings = bookings.size();
                            long activeBookings = bookings.stream()
                                    .filter(b -> "approved".equals(b.getStatus()) || "mixed".equals(b.getStatus()))
                                    .count();
                            long completedBookings = bookings.stream()
                                    .filter(b -> "completed".equals(b.getStatus()))
                                    .count();
                            
                            double totalSpent = bookings.stream()
                                    .filter(b -> "completed".equals(b.getStatus()))
                                    .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0.0)
                                    .sum();

                            return new Customer(
                                    customerUser,
                                    totalBookings,
                                    activeBookings,
                                    completedBookings,
                                    totalSpent,
                                    bookings
                            );
                        } catch (Exception e) {
                            log.error("Error processing customer {}: {}", customerId, e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(c -> c != null) // Remove null entries
                    .collect(Collectors.toList());

            model.addAttribute("customers", customers);
            model.addAttribute("totalCustomers", customers.size());
            
            log.info("Successfully loaded {} customers for host {}", customers.size(), host.getId());
            return "host/host-customers";
            
        } catch (Exception e) {
            log.error("Error loading customers for host {}: {}", host.getId(), e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách khách hàng. Vui lòng thử lại.");
            return "host/host-customers";
        }
    }

    @GetMapping("/host-customer-detail")
    public String showCustomerDetail(
            @RequestParam int customerId,
            HttpSession session,
            Model model) {
        
        User host = (User) session.getAttribute("user");

        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
            log.warn("Unauthorized access to customer detail page by user: {}", 
                host != null ? host.getId() : "null");
            return "redirect:/login";
        }

        try {
            log.info("Fetching customer detail for customer {} and host {}", customerId, host.getId());
            
            // Get customer details
            User customer = userService.findUserById(customerId);
            if (customer == null) {
                log.warn("Customer not found with ID: {}", customerId);
                return "redirect:/host-customers?error=customer-not-found";
            }

            // Get all bookings for this customer and host
            List<Booking> customerBookings = bookingService.getBookingsByHostId(host.getId())
                    .stream()
                    .filter(b -> b.getCustomerId() == customerId)
                    .collect(Collectors.toList());

            log.debug("Found {} bookings for customer {} and host {}", 
                customerBookings.size(), customerId, host.getId());

            // Calculate overall status for each booking
            for (Booking booking : customerBookings) {
                String overallStatus = bookingService.calculateBookingStatus(booking.getBookingUnits());
                booking.setStatus(overallStatus);
                double sum = booking.getBookingUnits().stream()
                        .filter(u -> u.getPrice() != null)
                        .mapToDouble(u -> u.getPrice() * (u.getQuantity() == 0 ? 1 : u.getQuantity()))
                        .sum();
                booking.setTotalPrice(sum);
            }

            model.addAttribute("customer", customer);
            model.addAttribute("bookings", customerBookings);
            model.addAttribute("currentUserId", host.getId());
            
            // Debug logging for template variables
            log.info("Setting template variables - currentUserId: {}, customer.id: {}", host.getId(), customer.getId());
            log.debug("Customer object: {}", customer);

            log.info("Successfully loaded customer detail for customer {} and host {}", customerId, host.getId());
            return "host/host-customer-detail";
            
        } catch (Exception e) {
            log.error("Error loading customer detail for customer {} and host {}: {}", 
                customerId, host.getId(), e.getMessage(), e);
            return "redirect:/host-customers?error=system-error";
        }
    }
} 