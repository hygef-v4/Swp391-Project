package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public void updateUserRole(int userId, String newRole) {
        userRepo.updateUserRoleById(userId, newRole);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepo.getAllUser();
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

    //  Update role to HOTEL_OWNER by user ID
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

    // Hàm cập nhật mật khẩu
    public void updateUserPassword(User sessionUser, String newPassword) {
        if (sessionUser == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại.");
        }

        // Mã hóa và cập nhật mật khẩu mới
        String encodedPassword = passwordEncoder.encode(newPassword);
        sessionUser.setPassword(encodedPassword);
        userRepo.updateUserPassword(sessionUser.getEmail(), encodedPassword);
    }


    public void validateUserProfile(String fullname, String phone, String dob, String bio) {
        // Kiểm tra trường fullname
        if (fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống.");
        }

        // Validate số điện thoại (chỉ chấp nhận 10-15 ký tự số)
        if (!phone.matches("\\d{10,15}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ. Chỉ chấp nhận từ 10-15 chữ số.");
        }

        // Kiểm tra ngày sinh
        if (dob == null || dob.isBlank()) {
            throw new IllegalArgumentException("Ngày sinh không được để trống.");
        }

        try {
            // Chuyển chuỗi ngày sinh (dob) sang LocalDate
            java.time.LocalDate birthDate = java.time.LocalDate.parse(dob);
            java.time.LocalDate today = java.time.LocalDate.now();

            // Kiểm tra xem ngày sinh có nằm trong tương lai không
            if (birthDate.isAfter(today)) {
                throw new IllegalArgumentException("Ngày sinh không được nằm trong tương lai.");
            }
        } catch (java.time.format.DateTimeParseException e) {
            // Bắt lỗi nếu ngày sinh không đúng định dạng
            throw new IllegalArgumentException("Ngày sinh không hợp lệ. Vui lòng nhập đúng định dạng mm/dd/yyyy.");
        }


        // Kiểm tra thông tin giới thiệu
        if ( bio.length() > 255) { // Không bắt buộc nhưng không được dài quá 255 ký tự
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
}
