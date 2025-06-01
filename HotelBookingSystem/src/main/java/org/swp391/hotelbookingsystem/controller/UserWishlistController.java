package org.swp391.hotelbookingsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.Favorites;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserWishlistRepository;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserWishlistController {

    @Autowired
    private UserWishlistRepository userWishlistRepository;

    @GetMapping("/user-wishlist")
    public String viewUserWishlist(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            // Nếu người dùng chưa đăng nhập, chuyển hướng về trang đăng nhập
            model.addAttribute("error", "Bạn cần đăng nhập để xem danh sách yêu thích.");
            return "redirect:/login";
        }

        try {
            // Lấy thông tin userId
            Integer userId = sessionUser.getId();

            // Lấy danh sách khách sạn yêu thích từ repository
            List<Favorites> favorites = userWishlistRepository.findFavoritesByUserId(userId);

            // Truyền danh sách xuống giao diện
            model.addAttribute("favorites", favorites);

            // Truyền thêm thông tin người dùng xuống giao diện nếu cần
            model.addAttribute("user", sessionUser);

        } catch (Exception e) {
            // Xử lý lỗi (nếu có)
            model.addAttribute("error", "Đã xảy ra lỗi khi tải danh sách yêu thích.");
            return "page/error"; // Hoặc trang báo lỗi tùy chọn
        }

        // Trả về trang danh sách yêu thích
        return "page/userWishlist";
    }
}