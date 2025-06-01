package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.dto.UserWithProfileDTO;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;

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

    public List<UserWithProfileDTO> searchUsersByName(String search) {
        return userRepo.searchUsersWithProfileByName(search);
    }

    public void toggleUserStatus(int userId) {
        User user = userRepo.findUserById(userId);
        if (user != null) {
            boolean newStatus = !user.isActive();
            userRepo.updateUserStatus(userId, newStatus);
        }
    }

    //  Update role to HOTEL OWNER by user ID
    public void updateUserRoleToHost(int userId) {
        userRepo.updateUserRoleById(userId, "HOTEL OWNER");
    }

    public List<User> getUsersByRole(String role) {
        return userRepo.getUsersByRole(role);
    }

    public List<UserWithProfileDTO> getAllUsersWithProfile() {
        return userRepo.getAllUsersWithProfile();
    }

    public List<UserWithProfileDTO> getTop5Users() {
        List<UserWithProfileDTO> allUsers = userRepo.getAllUsersWithProfile();
        return allUsers.size() > 5 ? allUsers.subList(0, 5) : allUsers;
    }
}
