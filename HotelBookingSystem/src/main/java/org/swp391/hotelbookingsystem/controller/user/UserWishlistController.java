package org.swp391.hotelbookingsystem.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Favorites;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.UserWishlistService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserWishlistController {

    @Autowired
    private UserWishlistService userWishlistService;

    @GetMapping("/user-wishlist")
    public String viewUserWishlist(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        try {
            // Lấy danh sách yêu thích từ service
            List<Favorites> favorites = userWishlistService.getUserWishlist(sessionUser);
            model.addAttribute("favorites", favorites);
            model.addAttribute("user", sessionUser);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải danh sách yêu thích.");
            return "redirect:/error";
        }

        return "page/userWishlist";
    }

    @PostMapping("/remove-favorite")
    public String removeFavorite(@RequestParam("hotelId") int hotelId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        try {
            // Xóa khách sạn khỏi danh sách yêu thích
            userWishlistService.removeFavorite(sessionUser, hotelId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công mục yêu thích.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa mục yêu thích.");
        }

        return "redirect:/user-wishlist";
    }

    @PostMapping("/add-favorite")
    public String addFavorite(@RequestParam("hotelId") int hotelId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");

        try {
            // Thêm khách sạn vào danh sách yêu thích
            userWishlistService.addFavorite(sessionUser, hotelId);
            redirectAttributes.addFlashAttribute("successMessage", "Khách sạn đã được thêm vào danh sách yêu thích.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xảy ra lỗi khi thêm vào danh sách yêu thích.");
        }

        return "redirect:/hotel-detail?hotelId=" + hotelId;
    }
}