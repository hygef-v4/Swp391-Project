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
                    // Calculate booking status based on booking units
                    String status = bookingService.calculateBookingStatus(booking.getBookingUnits());
                    booking.setStatus(status);
                    
                    // Calculate total price - only count approved, completed, and check_in booking units for revenue
                    double sum = booking.getBookingUnits().stream()
                            .filter(u -> u.getPrice() != null && u.getPrice() > 0)
                            .filter(u -> {
                                String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                return "approved".equals(unitStatus) || "completed".equals(unitStatus) || "check_in".equals(unitStatus);
                            })
                            .mapToDouble(u -> {
                                int qty = u.getQuantity() <= 0 ? 1 : u.getQuantity();
                                return u.getPrice() * qty;
                            })
                            .sum();
                    booking.setTotalPrice(sum);
                    
                    log.debug("Booking {} - price: {}, status: {}, units: {}", 
                        booking.getBookingId(), sum, status, booking.getBookingUnits().size());
                } else {
                    booking.setTotalPrice(0.0);
                    booking.setStatus("unknown");
                    log.warn("Booking {} has no booking units", booking.getBookingId());
                }
            }

            // Filter bookings to only include customers with at least one approved/completed/check_in booking
            List<Booking> validBookings = allBookings.stream()
                    .filter(booking -> {
                        String status = booking.getStatus();
                        return "approved".equals(status) || "completed".equals(status) || "check_in".equals(status);
                    })
                    .collect(Collectors.toList());

            log.debug("Found {} valid bookings (approved/completed/check_in) for host {}", validBookings.size(), host.getId());

            // Group valid bookings by customer - only customers with approved+ bookings can chat
            Map<Integer, List<Booking>> customerBookings = validBookings.stream()
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
                            
                            // Calculate stats - only count valid bookings
                            long totalBookings = bookings.size();
                            long activeBookings = bookings.stream()
                                    .filter(b -> "approved".equals(b.getStatus()))
                                    .count();
                            long completedBookings = bookings.stream()
                                    .filter(b -> "completed".equals(b.getStatus()))
                                    .count();
                            
                            // Calculate total revenue from valid bookings
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
                            
                            log.info("Customer {} stats - Total valid bookings: {}, Active: {}, Completed: {}, Revenue: {}", 
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
            
            log.info("Successfully loaded {} customers for host {} (filtered for valid bookings only)", customers.size(), host.getId());
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
            List<Booking> allCustomerBookings = bookingService.getBookingsByHostId(host.getId())
                    .stream()
                    .filter(b -> b.getCustomerId() == customerId)
                    .collect(Collectors.toList());

            // Calculate status for all bookings and filter out pending booking units
            for (Booking booking : allCustomerBookings) {
                if (booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty()) {
                    // Filter out pending booking units for display
                    List<org.swp391.hotelbookingsystem.model.BookingUnit> nonPendingUnits = booking.getBookingUnits().stream()
                            .filter(u -> {
                                String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                return !"pending".equals(unitStatus);
                            })
                            .collect(Collectors.toList());
                    
                    // Set the filtered booking units
                    booking.setBookingUnits(nonPendingUnits);
                    
                    // Calculate status based on filtered units
                    if (!nonPendingUnits.isEmpty()) {
                        String status = bookingService.calculateBookingStatus(nonPendingUnits);
                        booking.setStatus(status);
                        
                        double sum = nonPendingUnits.stream()
                                .filter(u -> u.getPrice() != null && u.getPrice() > 0)
                                .filter(u -> {
                                    String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                    return "approved".equals(unitStatus) || "completed".equals(unitStatus) || "check_in".equals(unitStatus);
                                })
                                .mapToDouble(u -> u.getPrice() * (u.getQuantity() == 0 ? 1 : u.getQuantity()))
                                .sum();
                        booking.setTotalPrice(sum);
                    } else {
                        // If no non-pending units, set booking as having no valid units
                        booking.setTotalPrice(0.0);
                        booking.setStatus("no_valid_units");
                    }
                } else {
                    booking.setTotalPrice(0.0);
                    booking.setStatus("unknown");
                }
            }

            // Filter out bookings that have no valid booking units after removing pending ones
            allCustomerBookings = allCustomerBookings.stream()
                    .filter(booking -> {
                        return booking.getBookingUnits() != null && 
                               !booking.getBookingUnits().isEmpty() && 
                               !"no_valid_units".equals(booking.getStatus());
                    })
                    .collect(Collectors.toList());

            // Check if customer has at least one approved/completed/check_in booking - block access if only pending
            boolean hasValidBooking = allCustomerBookings.stream()
                    .anyMatch(booking -> {
                        String status = booking.getStatus();
                        return "approved".equals(status) || "completed".equals(status) || "check_in".equals(status);
                    });

            if (!hasValidBooking) {
                log.warn("Customer {} blocked from chat - only has pending bookings with host {}", customerId, host.getId());
                return "redirect:/host-customers?error=customer-no-valid-bookings";
            }

            log.debug("Found {} bookings for customer {} and host {}", 
                allCustomerBookings.size(), customerId, host.getId());

            model.addAttribute("customer", customer);
            model.addAttribute("bookings", allCustomerBookings);
            model.addAttribute("currentUserId", host.getId());
            
            log.info("Successfully loaded customer detail for customer {} and host {}", customerId, host.getId());
            return "host/host-customer-detail";
            
        } catch (Exception e) {
            log.error("Error loading customer detail for customer {} and host {}: {}", 
                customerId, host.getId(), e.getMessage(), e);
            return "redirect:/host-customers?error=system-error";
        }
    }
}
