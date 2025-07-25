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

@Controller
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
            return "redirect:/login";
        }

        try {
            // Get all bookings for this host
            List<Booking> allBookings = bookingService.getBookingsByHostId(host.getId());

            // Calculate status and total price for each booking
            for (Booking booking : allBookings) {
                if (booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty()) {
                    // Calculate booking status based on booking units
                    String status = bookingService.calculateBookingStatus(booking.getBookingUnits());
                    booking.setStatus(status);

                    // Calculate total price correctly including number of days
                    booking.calculateNumberOfNights();
                    double calculatedPrice = booking.calculateTotalPrice();
                    booking.setTotalPrice(calculatedPrice);
                    

                } else {
                    booking.setTotalPrice(0.0);
                    booking.setStatus("unknown");

                }
            }

            // Filter bookings to only include customers with at least one approved/completed/check_in booking
            List<Booking> validBookings = allBookings.stream()
                    .filter(booking -> {
                        String status = booking.getStatus();
                        return "approved".equals(status) || "completed".equals(status) || "check_in".equals(status);
                    })
                    .collect(Collectors.toList());



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

                                        return price;
                                    })
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
                            return null;
                        }
                    })
                    .filter(c -> c != null) // Remove null entries
                    .collect(Collectors.toList());

            model.addAttribute("customers", customers);
            model.addAttribute("totalCustomers", customers.size());
            

            return "host/host-customers";
            
        } catch (Exception e) {
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

//        if (host == null || !host.getRole().equalsIgnoreCase("HOTEL_OWNER")) {
//            return "redirect:/login";
//        }

        try {
            // Get customer details
            User customer = userService.findUserById(customerId);
            if (customer == null) {
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

                        // Calculate total price correctly including number of days
                        booking.calculateNumberOfNights();
                        double calculatedPrice = booking.calculateTotalPrice();
                        booking.setTotalPrice(calculatedPrice);

                        // Check if booking has revenue-generating units
                        boolean hasRevenueUnits = nonPendingUnits.stream()
                                .anyMatch(u -> {
                                    String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                    return "approved".equals(unitStatus) || "completed".equals(unitStatus) || "check_in".equals(unitStatus);
                                });

                        if (!hasRevenueUnits) {
                            booking.setTotalPrice(0.0);
                        }
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
                return "redirect:/host-customers?error=customer-no-valid-bookings";
            }

            model.addAttribute("customer", customer);
            model.addAttribute("bookings", allCustomerBookings);
            model.addAttribute("currentUserId", host.getId());
            

            return "host/host-customer-detail";
            
        } catch (Exception e) {
            return "redirect:/host-customers?error=system-error";
        }
    }
}
