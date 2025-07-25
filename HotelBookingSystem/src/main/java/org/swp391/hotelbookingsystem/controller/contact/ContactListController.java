package org.swp391.hotelbookingsystem.controller.contact;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.HotelService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ContactListController {

    private final UserService userService;
    private final HotelService hotelService;

    public ContactListController(UserService userService, HotelService hotelService) {
        this.userService = userService;
        this.hotelService = hotelService;
    }

    // Customer views list of moderators to contact
    @GetMapping("/customer-moderators")
    public String customerModerators(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "") String search,
                                    Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"CUSTOMER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ khách hàng mới có thể xem danh sách này.");
        }

        List<User> moderators = userService.getUsersByRole("MODERATOR");

        // Simple search filter
        if (!search.isEmpty()) {
            moderators = moderators.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = moderators.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedModerators = moderators.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedModerators);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Moderator");
        model.addAttribute("contactType", "customer-moderator");

        return "contact/contact-list";
    }

    // Customer views list of admins to contact
    @GetMapping("/customer-admins")
    public String customerAdmins(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "") String search,
                                Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"CUSTOMER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ khách hàng mới có thể xem danh sách này.");
        }

        List<User> admins = userService.getUsersByRole("ADMIN");

        // Simple search filter
        if (!search.isEmpty()) {
            admins = admins.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = admins.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedAdmins = admins.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedAdmins);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Admin");
        model.addAttribute("contactType", "customer-admin");

        return "contact/contact-list";
    }

    // Moderator views list of customers to contact
    @GetMapping("/moderator-contact-users")
    public String moderatorContactUsers(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "") String search,
                                       Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        
        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ moderator mới có thể xem danh sách này.");
        }

        List<User> customers = userService.getUsersByRole("CUSTOMER");
        
        // Simple search filter
        if (!search.isEmpty()) {
            customers = customers.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = customers.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        
        List<User> paginatedCustomers = customers.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedCustomers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách người dùng");
        model.addAttribute("contactType", "moderator-customer");
        
        return "contact/contact-list";
    }

    // Hotel owner views list of moderators to contact
    @GetMapping("/host-moderators")
    public String hostModerators(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "") String search,
                                Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"HOTEL_OWNER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ chủ khách sạn mới có thể xem danh sách này.");
        }

        List<User> moderators = userService.getUsersByRole("MODERATOR");

        // Simple search filter
        if (!search.isEmpty()) {
            moderators = moderators.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = moderators.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedModerators = moderators.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedModerators);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Kiểm duyệt viên");
        model.addAttribute("contactType", "host-moderator");

        return "contact/contact-list";
    }

    // Hotel owner views list of admins to contact
    @GetMapping("/host-admins")
    public String hostAdmins(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "") String search,
                            Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"HOTEL_OWNER".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ chủ khách sạn mới có thể xem danh sách này.");
        }

        List<User> admins = userService.getUsersByRole("ADMIN");

        // Simple search filter
        if (!search.isEmpty()) {
            admins = admins.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = admins.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedAdmins = admins.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedAdmins);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Admin");
        model.addAttribute("contactType", "host-admin");

        return "contact/contact-list";
    }

    // Admin views list of hotel owners to contact
    @GetMapping("/admin-hosts")
    public String adminHosts(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "") String search,
                            Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể xem danh sách này.");
        }

        List<User> hotelOwners = userService.getUsersByRole("HOTEL_OWNER");

        // Simple search filter
        if (!search.isEmpty()) {
            hotelOwners = hotelOwners.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = hotelOwners.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedHosts = hotelOwners.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedHosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Chủ khách sạn");
        model.addAttribute("contactType", "admin-host");

        return "contact/contact-list";
    }

    // Admin views list of customers to contact
    @GetMapping("/admin-customers")
    public String adminCustomers(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "") String search,
                                Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể xem danh sách này.");
        }

        List<User> customers = userService.getUsersByRole("CUSTOMER");

        // Simple search filter
        if (!search.isEmpty()) {
            customers = customers.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = customers.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedCustomers = customers.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedCustomers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Khách hàng");
        model.addAttribute("contactType", "admin-customer");

        return "contact/contact-list";
    }

    // Moderator views list of hotel owners to contact
    @GetMapping("/moderator-hosts")
    public String moderatorHosts(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "") String search,
                                Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ kiểm duyệt viên mới có thể xem danh sách này.");
        }

        List<User> hotelOwners = userService.getUsersByRole("HOTEL_OWNER");

        // Simple search filter
        if (!search.isEmpty()) {
            hotelOwners = hotelOwners.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = hotelOwners.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedHosts = hotelOwners.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedHosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Chủ khách sạn");
        model.addAttribute("contactType", "moderator-host");

        return "contact/contact-list";
    }

    // Moderator views list of admins to contact
    @GetMapping("/moderator-admins")
    public String moderatorAdmins(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "") String search,
                                 Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"MODERATOR".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ kiểm duyệt viên mới có thể xem danh sách này.");
        }

        List<User> admins = userService.getUsersByRole("ADMIN");

        // Simple search filter
        if (!search.isEmpty()) {
            admins = admins.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = admins.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedAdmins = admins.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedAdmins);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Admin");
        model.addAttribute("contactType", "moderator-admin");

        return "contact/contact-list";
    }

    // Admin views list of moderators to contact
    @GetMapping("/admin-moderators")
    public String adminModerators(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "") String search,
                                 Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("Chỉ admin mới có thể xem danh sách này.");
        }

        List<User> moderators = userService.getUsersByRole("MODERATOR");

        // Simple search filter
        if (!search.isEmpty()) {
            moderators = moderators.stream()
                .filter(user -> user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }

        // Simple pagination
        int pageSize = 10;
        int totalCount = moderators.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<User> paginatedModerators = moderators.subList(startIndex, endIndex);

        model.addAttribute("users", paginatedModerators);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Danh sách Kiểm duyệt viên");
        model.addAttribute("contactType", "admin-moderator");

        return "contact/contact-list";
    }
}
