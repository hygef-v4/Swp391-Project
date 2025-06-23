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

            // Calculate total price and status for each booking
            for (Booking booking : allBookings) {
                if (booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty()) {
                    // Calculate total price - only count approved and completed booking units
                    double sum = booking.getBookingUnits().stream()
                            .filter(u -> u.getPrice() != null && u.getPrice() > 0)
                            .filter(u -> {
                                String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                return "approved".equals(unitStatus) || "completed".equals(unitStatus);
                            })
                            .mapToDouble(u -> {
                                int qty = u.getQuantity() <= 0 ? 1 : u.getQuantity();
                                return u.getPrice() * qty;
                            })
                            .sum();
                    booking.setTotalPrice(sum);
                    
                    // Calculate booking status based on booking units
                    String status = bookingService.calculateBookingStatus(booking.getBookingUnits());
                    booking.setStatus(status);
                    
                    log.debug("Booking {} - price: {}, status: {}, units: {}", 
                        booking.getBookingId(), sum, status, booking.getBookingUnits().size());
                } else {
                    booking.setTotalPrice(0.0);
                    booking.setStatus("unknown");
                    log.warn("Booking {} has no booking units", booking.getBookingId());
                }
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
                                    .filter(b -> "approved".equals(b.getStatus()))
                                    .count();
                            long completedBookings = bookings.stream()
                                    .filter(b -> "completed".equals(b.getStatus()))
                                    .count();
                            
                            // Calculate total revenue from all bookings (totalPrice already filtered for approved/completed units)
                            double totalSpent = bookings.stream()
                                    .mapToDouble(b -> {
                                        double price = b.getTotalPrice() != null ? b.getTotalPrice() : 0.0;
                                        if (price > 0) {
                                            log.debug("Customer {} booking {} contributes {} to revenue", 
                                                customerId, b.getBookingId(), price);
                                        }
                                        return price;
                                    })
                                    .sum();
                            
                            log.info("Customer {} stats - Total bookings: {}, Active: {}, Completed: {}, Revenue: {}", 
                                customerId, totalBookings, activeBookings, completedBookings, totalSpent);

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

            // Calculate total price and status for each booking
            for (Booking booking : customerBookings) {
                if (booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty()) {
                    // Calculate total price - only count approved and completed booking units
                    double sum = booking.getBookingUnits().stream()
                            .filter(u -> u.getPrice() != null && u.getPrice() > 0)
                            .filter(u -> {
                                String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                return "approved".equals(unitStatus) || "completed".equals(unitStatus);
                            })
                            .mapToDouble(u -> {
                                int qty = u.getQuantity() <= 0 ? 1 : u.getQuantity();
                                return u.getPrice() * qty;
                            })
                            .sum();
                    booking.setTotalPrice(sum);
                    
                    // Calculate booking status
                    String status = bookingService.calculateBookingStatus(booking.getBookingUnits());
                    booking.setStatus(status);
                    
                    log.debug("Customer detail - Booking {} - price: {}, status: {}", 
                        booking.getBookingId(), sum, status);
                } else {
                    booking.setTotalPrice(0.0);
                    booking.setStatus("unknown");
                    log.warn("Customer detail - Booking {} has no booking units", booking.getBookingId());
                }
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