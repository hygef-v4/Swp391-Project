package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.ArrayList;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swp391.hotelbookingsystem.service.ReportService;
import org.swp391.hotelbookingsystem.model.Report;
import org.swp391.hotelbookingsystem.service.BankService;
import org.swp391.hotelbookingsystem.model.Bank;

@Service
public class UserService {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final ReportService reportService;

    private final BankService bankService;

    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, ReportService reportService, BankService bankService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
        this.bankService = bankService;
    }

    public void updateUserRole(int userId, String newRole) {
        userRepo.updateUserRoleById(userId, newRole);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User findUserById(int userId) {
        return userRepo.findUserById(userId);
    }

    public List<User> getAgentsBySearchPaginated(String keyword, int offset, int size) {
        return userRepo.findUsersByRoleAndSearchPaginated("HOTEL_OWNER", keyword, offset, size);
    }

    public int countAgentsBySearch(String keyword) {
        return userRepo.countUsersByRoleAndSearch("HOTEL_OWNER", keyword);
    }

    public List<User> getAgentsSortedPaginated(String search, String sort, int offset, int size) {
        return userRepo.findAgentsBySearchSorted(search, sort, offset, size);
    }

    public List<User> searchUsersByName(String search) {
        return userRepo.searchUsersWithProfileByName(search);
    }

    public void toggleUserStatus(int userId) {
        User user = userRepo.findUserById(userId);
        if (user != null) {
            boolean newStatus = !user.isActive();
            userRepo.updateUserStatus(userId, newStatus);
        }
    }

    public int countAgent() {
        return userRepo.countAgent();
    }

    public void updateUserRoleToHost(int userId) {
        userRepo.updateUserRoleById(userId, "HOTEL_OWNER");
    }

    public List<User> getUsersByRole(String role) {
        return userRepo.getUsersByRole(role);
    }

    public List<User> getAllUsersWithProfile() {
        return userRepo.getAllUsersWithProfile();
    }

    public List<User> getTop5Users() {
        List<User> allUsers = userRepo.getAllUsersWithProfile();
        return allUsers.size() > 5 ? allUsers.subList(0, 5) : allUsers;
    }

    /**
     * Validates if a user's profile is complete enough for hotel owner registration
     * @param user The user to validate
     * @return ProfileValidationResult containing validation status and missing fields
     */
    public ProfileValidationResult validateProfileCompleteness(User user) {
        List<String> missingFields = new ArrayList<>();

        if (user == null) {
            missingFields.add("Thông tin người dùng");
            return new ProfileValidationResult(false, missingFields, "Người dùng không tồn tại");
        }

        // Debug logging - remove in production
        System.out.println("=== PROFILE VALIDATION DEBUG ===");
        System.out.println("Full Name: '" + user.getFullName() + "'");
        System.out.println("Phone: '" + user.getPhone() + "'");
        System.out.println("DOB: " + user.getDob());
        System.out.println("Avatar URL: '" + user.getAvatarUrl() + "'");

        // Check required fields for hotel owner registration
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            missingFields.add("Tên đầy đủ");
            System.out.println("Missing: Full Name");
        }

        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            missingFields.add("Số điện thoại");
            System.out.println("Missing: Phone (empty)");
        } else {
            String cleanPhone = user.getPhone().trim();
            if (!cleanPhone.matches("\\d{10,15}")) {
                missingFields.add("Số điện thoại hợp lệ (10-15 chữ số)");
                System.out.println("Missing: Phone (invalid format) - '" + cleanPhone + "'");
            }
        }

        if (user.getDob() == null) {
            missingFields.add("Ngày sinh");
            System.out.println("Missing: Date of Birth");
        }

        // Avatar is recommended for hotel owners for trust and credibility
        if (user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty() ||
            user.getAvatarUrl().equals("/assets/images/avatar/avatar.jpg") ||
            user.getAvatarUrl().contains("avatar.jpg")) {
            missingFields.add("Ảnh đại diện");
        }

        // Payment information is required for hotel owners to receive payments
        List<Bank> userBanks = bankService.getUserBanks(user.getId());
        if (userBanks == null || userBanks.isEmpty()) {
            missingFields.add("Thông tin thanh toán");
        }

        boolean isComplete = missingFields.isEmpty();
        String message = isComplete ?
            "Hồ sơ đã đầy đủ" :
            "Vui lòng hoàn thiện các thông tin sau: " + String.join(", ", missingFields);

        return new ProfileValidationResult(isComplete, missingFields, message);
    }

    /**
     * Check if user has payment information set up
     * @param userId The user ID to check
     * @return true if user has at least one bank account, false otherwise
     */
    public boolean hasPaymentInformation(int userId) {
        List<Bank> userBanks = bankService.getUserBanks(userId);
        return userBanks != null && !userBanks.isEmpty();
    }

    public void saveEmailOtpToken(String email, String otp) {
        userRepo.saveEmailOtpToken(email, otp);
    }

    public boolean isValidEmailOtp(String email, String otp) {
        return userRepo.isValidEmailOtp(email, otp);
    }

    public void saveUser(User user) {
        userRepo.saveUser(user);
    }

    public void deleteEmailOtp(String email, String otp) {
        userRepo.deleteEmailOtp(email, otp);
    }

    public void savePasswordResetToken(int id, String token) {
        userRepo.savePasswordResetToken(id, token);
    }

    public User findUserByToken(String token) {
        return userRepo.findUserByToken(token);
    }

    public void updatePassword(int id, String hashed) {
        userRepo.updatePassword(id, hashed);
    }

    public void deleteToken(String token) {
        userRepo.deleteToken(token);
    }

    public void updateUser(User user) {
        userRepo.updateUser(user);
    }

    public void updateUserPassword(User sessionUser, String newPassword) {
        if (sessionUser == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        sessionUser.setPassword(encodedPassword);
        userRepo.updateUserPassword(sessionUser.getEmail(), encodedPassword);
    }


    public void validateUserProfile(String fullname, String phone, String dob, String bio) {

        if (fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống.");
        }

        if (!phone.matches("\\d{10,15}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ. Chỉ chấp nhận từ 10-15 chữ số.");
        }

        if (dob == null || dob.isBlank()) {
            throw new IllegalArgumentException("Ngày sinh không được để trống.");
        }

        try {
            java.time.LocalDate birthDate = java.time.LocalDate.parse(dob);
            java.time.LocalDate today = java.time.LocalDate.now();

            if (birthDate.isAfter(today)) {
                throw new IllegalArgumentException("Ngày sinh không được nằm trong tương lai.");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ. Vui lòng nhập đúng định dạng mm/dd/yyyy.");
        }

        if ( bio.length() > 255) {
            throw new IllegalArgumentException("Thông tin giới thiệu không được dài quá 255 ký tự.");
        }


    }

    public void validatePasswordChange(User sessionUser, String currentPassword, String newPassword, String confirmPassword) {
        // Kiểm tra người dùng có đăng nhập hay không
        if (sessionUser == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập!");
        }

        // Nếu mật khẩu đã được thiết lập, kiểm tra mật khẩu hiện tại
        if (sessionUser.getPassword() != null && !passwordEncoder.matches(currentPassword, sessionUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        // Kiểm tra nếu mật khẩu mới và xác nhận mật khẩu không khớp
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        // Kiểm tra chiều dài mật khẩu mới
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        }

        // Kiểm tra nếu mật khẩu mới trùng với mật khẩu hiện tại
        if (passwordEncoder.matches(newPassword, sessionUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
        }

    }

    public void deleteEmailOtp(String email) {
        userRepo.deleteEmailOtp(email);
    }

    // Thêm method để tính tổng số người dùng
    public int getTotalUsers() {
        List<User> allUsers = userRepo.getAllUser();
        return allUsers != null ? allUsers.size() : 0;
    }

    // New: Check if user is flagged (pending report)
    public boolean isUserFlagged(int userId) {
        return reportService.isUserFlagged(userId);
    }
    // New: Get latest pending report for user
    public Report getLatestPendingReport(int userId) {
        return reportService.getLatestPendingReport(userId);
    }
    // New: Flag user
    public void flagUser(int reporterId, int reportedUserId, String reason) {
        reportService.flagUser(reporterId, reportedUserId, reason);
    }
    // New: Unflag user
    public void unflagUser(int userId) {
        reportService.unflagUser(userId);
    }

    /**
     * Inner class to hold profile validation results
     */
    public static class ProfileValidationResult {
        private final boolean isComplete;
        private final List<String> missingFields;
        private final String message;

        public ProfileValidationResult(boolean isComplete, List<String> missingFields, String message) {
            this.isComplete = isComplete;
            this.missingFields = missingFields;
            this.message = message;
        }

        public boolean isComplete() { return isComplete; }
        public List<String> getMissingFields() { return missingFields; }
        public String getMessage() { return message; }
    }

    public void banUser(int userId) {
        userRepo.banUserById(userId);
    }

}


