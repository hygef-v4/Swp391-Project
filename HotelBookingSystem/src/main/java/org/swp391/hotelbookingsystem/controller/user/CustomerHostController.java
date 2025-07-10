package org.swp391.hotelbookingsystem.controller.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class CustomerHostController {

    private final BookingService bookingService;
    private final UserService userService;
    private final HotelService hotelService;

    public CustomerHostController(BookingService bookingService, UserService userService, HotelService hotelService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.hotelService = hotelService;
    }

    @GetMapping("/customer-hosts")
    public String showHostsForCustomer(HttpSession session, Model model) {
        User customer = (User) session.getAttribute("user");

        if (customer == null || !customer.getRole().equalsIgnoreCase("CUSTOMER")) {
            log.warn("Unauthorized access to customer hosts page by user: {}", 
                customer != null ? customer.getId() : "null");
            return "redirect:/login";
        }

        try {
            log.info("Fetching hosts for customer: {}", customer.getId());
            
            // Get all bookings for this customer
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Booking> customerBookings = allBookings.stream()
                    .filter(b -> b.getCustomerId() == customer.getId())
                    .collect(Collectors.toList());
            
            log.debug("Found {} bookings for customer {}", customerBookings.size(), customer.getId());

            // Calculate booking status for all bookings
            for (Booking booking : customerBookings) {
                if (booking.getBookingUnits() != null && !booking.getBookingUnits().isEmpty()) {
                    String status = bookingService.calculateBookingStatus(booking.getBookingUnits());
                    booking.setStatus(status);
                } else {
                    booking.setStatus("unknown");
                }
            }

            // Filter to only include bookings with approved/completed/check_in status
            List<Booking> validBookings = customerBookings.stream()
                    .filter(booking -> {
                        String status = booking.getStatus();
                        return "approved".equals(status) || "completed".equals(status) || "check_in".equals(status);
                    })
                    .collect(Collectors.toList());

            log.debug("Found {} valid bookings (approved/completed/check_in) for customer {}", validBookings.size(), customer.getId());

            // Get unique hotel IDs from valid bookings only
            List<Integer> hotelIds = validBookings.stream()
                    .map(Booking::getHotelId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // Get host IDs from hotels
            List<Integer> hostIds = hotelIds.stream()
                    .map(hotelId -> {
                        Hotel hotel = hotelService.getHotelById(hotelId);
                        return hotel != null ? hotel.getHostId() : null;
                    })
                    .filter(hostId -> hostId != null)
                    .distinct()
                    .collect(Collectors.toList());

            // Get host information and their hotels
            List<HostInfo> hosts = hostIds.stream()
                    .map(hostId -> {
                        try {
                            User host = userService.findUserById(hostId);
                            if (host == null) {
                                log.warn("Host not found with ID: {}", hostId);
                                return null;
                            }
                            
                            // Get hotels for this host
                            List<Hotel> hostHotels = hotelService.getHotelsByHostId(hostId);
                            
                            // Calculate stats for this host - only count valid bookings
                            List<Booking> hostValidBookings = validBookings.stream()
                                    .filter(b -> {
                                        Hotel hotel = hotelService.getHotelById(b.getHotelId());
                                        return hotel != null && hotel.getHostId() == hostId;
                                    })
                                    .collect(Collectors.toList());
                            
                            long totalBookings = hostValidBookings.size();
                            
                            // Calculate total spent - only count approved, completed, and check_in booking units for revenue
                            double totalSpent = hostValidBookings.stream()
                                    .mapToDouble(b -> {
                                        if (b.getBookingUnits() == null || b.getBookingUnits().isEmpty()) {
                                            return 0.0;
                                        }
                                        
                                        // Only count approved, completed, and check_in booking units
                                        double sum = b.getBookingUnits().stream()
                                                .filter(u -> u.getPrice() != null && u.getPrice() > 0)
                                                .filter(u -> {
                                                    String unitStatus = (u.getStatus() != null) ? u.getStatus().toLowerCase() : "";
                                                    return "approved".equals(unitStatus) || "completed".equals(unitStatus) || "check_in".equals(unitStatus);
                                                })
                                                .mapToDouble(u -> u.getPrice() * (u.getQuantity() == 0 ? 1 : u.getQuantity()))
                                                .sum();
                                        return sum;
                                    })
                                    .sum();

                            return new HostInfo(host, hostHotels, totalBookings, totalSpent, hostValidBookings);
                        } catch (Exception e) {
                            log.error("Error processing host {}: {}", hostId, e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(h -> h != null)
                    .collect(Collectors.toList());

            // Pre-calculate totals for the template
            long totalBookings = hosts.stream()
                    .mapToLong(HostInfo::getTotalBookings)
                    .sum();
            
            int totalHotels = hosts.stream()
                    .mapToInt(h -> h.getHotels().size())
                    .sum();
            
            model.addAttribute("hosts", hosts);
            model.addAttribute("totalHosts", hosts.size());
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalHotels", totalHotels);
            
            log.info("Successfully loaded {} hosts for customer {} (filtered for valid bookings only)", hosts.size(), customer.getId());
            return "user/customer-hosts";
            
        } catch (Exception e) {
            log.error("Error loading hosts for customer {}: {}", customer.getId(), e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách chủ khách sạn. Vui lòng thử lại.");
            return "user/customer-hosts";
        }
    }

    @GetMapping("/customer-host-detail")
    public String showHostDetail(
            @RequestParam int hostId,
            HttpSession session,
            Model model) {
        
        User customer = (User) session.getAttribute("user");

        if (customer == null || !customer.getRole().equalsIgnoreCase("CUSTOMER")) {
            log.warn("Unauthorized access to host detail page by user: {}", 
                customer != null ? customer.getId() : "null");
            return "redirect:/login";
        }

        try {
            log.info("Fetching host detail for host {} and customer {}", hostId, customer.getId());
            
            // Get host details
            User host = userService.findUserById(hostId);
            if (host == null) {
                log.warn("Host not found with ID: {}", hostId);
                return "redirect:/customer-hosts?error=host-not-found";
            }

            // Get all bookings for this customer and host
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Booking> allCustomerBookings = allBookings.stream()
                    .filter(b -> b.getCustomerId() == customer.getId())
                    .collect(Collectors.toList());
            
            List<Booking> customerBookings = allCustomerBookings.stream()
                    .filter(b -> {
                        Hotel hotel = hotelService.getHotelById(b.getHotelId());
                        return hotel != null && hotel.getHostId() == hostId;
                    })
                    .sorted((b1, b2) -> Integer.compare(b2.getBookingId(), b1.getBookingId())) // Sort by booking ID descending
                    .collect(Collectors.toList());

            log.debug("Found {} bookings for customer {} and host {}", 
                customerBookings.size(), customer.getId(), hostId);

            // Calculate status for all bookings and filter out pending booking units
            for (Booking booking : customerBookings) {
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

            // Check if customer has at least one approved/completed/check_in booking with this host - block access if only pending
            boolean hasValidBooking = customerBookings.stream()
                    .anyMatch(booking -> {
                        String status = booking.getStatus();
                        return "approved".equals(status) || "completed".equals(status) || "check_in".equals(status);
                    });

            if (!hasValidBooking) {
                log.warn("Customer {} blocked from chat - only has pending bookings with host {}", customer.getId(), hostId);
                return "redirect:/customer-hosts?error=no-valid-bookings";
            }

            // Get host hotels for additional context
            List<Hotel> hostHotels = hotelService.getHotelsByHostId(hostId);

            model.addAttribute("host", host);
            model.addAttribute("hostHotels", hostHotels);
            model.addAttribute("bookings", customerBookings);
            model.addAttribute("currentUserId", customer.getId());
            
            log.info("Successfully loaded host detail for host {} and customer {}", hostId, customer.getId());
            return "user/customer-host-detail";
            
        } catch (Exception e) {
            log.error("Error loading host detail for host {} and customer {}: {}", 
                hostId, customer.getId(), e.getMessage(), e);
            return "redirect:/customer-hosts?error=system-error";
        }
    }

    // Inner class to hold host information
    public static class HostInfo {
        private User host;
        private List<Hotel> hotels;
        private long totalBookings;
        private double totalSpent;
        private List<Booking> bookings;

        public HostInfo(User host, List<Hotel> hotels, long totalBookings, double totalSpent, List<Booking> bookings) {
            this.host = host;
            this.hotels = hotels;
            this.totalBookings = totalBookings;
            this.totalSpent = totalSpent;
            this.bookings = bookings;
        }

        // Getters
        public User getHost() { return host; }
        public List<Hotel> getHotels() { return hotels; }
        public long getTotalBookings() { return totalBookings; }
        public double getTotalSpent() { return totalSpent; }
        public List<Booking> getBookings() { return bookings; }
    }
} 