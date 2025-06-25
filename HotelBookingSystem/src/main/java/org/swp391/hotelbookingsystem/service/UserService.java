package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
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

    public void flagUser(int userId, String reason) {
        if (reason == null) {
            userRepo.flagUserById(userId, null); // truyền null để huỷ flag
        } else {
            userRepo.flagUserById(userId, reason);
        }
    }

}


