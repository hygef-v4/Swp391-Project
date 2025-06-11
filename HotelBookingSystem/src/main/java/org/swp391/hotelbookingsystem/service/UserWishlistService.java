package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Favorites;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserWishlistRepository;

import java.util.List;

@Service
public class UserWishlistService {

    private final UserWishlistRepository userWishlistRepository;

    @Autowired
    public UserWishlistService(UserWishlistRepository userWishlistRepository) {
        this.userWishlistRepository = userWishlistRepository;
    }

    public void validateUserSession(User sessionUser) {
        if (sessionUser == null) {
            throw new IllegalStateException("Bạn cần đăng nhập để thực hiện hành động này.");
        }
    }

    public List<Favorites> getUserWishlist(User sessionUser) {
        validateUserSession(sessionUser);
        return userWishlistRepository.findFavoritesByUserId(sessionUser.getId());
    }

    public void addFavorite(User sessionUser, int hotelId) {
        validateUserSession(sessionUser);
        try {
            userWishlistRepository.addFavorite(sessionUser.getId(), hotelId);
        } catch (Exception e) {
            throw new IllegalStateException("Xảy ra lỗi khi thêm vào danh sách yêu thích.");
        }
    }


    public void removeFavorite(User sessionUser, int hotelId) {
        validateUserSession(sessionUser);
        try {
            userWishlistRepository.deleteFavorite(sessionUser.getId(), hotelId);
        } catch (Exception e) {
            throw new IllegalStateException("Đã xảy ra lỗi khi xóa mục yêu thích.");
        }
    }
}
