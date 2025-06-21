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
import org.swp391.hotelbookingsystem.repository.UserWishlistRepository;

import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class UserWishlistController {

    private final UserWishlistRepository userWishlistRepository;

    public UserWishlistController(UserWishlistRepository userWishlistRepository) {
        this.userWishlistRepository = userWishlistRepository;
    }

    @GetMapping("/user-wishlist")
    public String viewUserWishlist(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            // Nếu người dùng chưa đăng nhập, chuyển hướng về trang đăng nhập
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện hành động này.");
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
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải danh sách yêu thích.");
            return "redirect:/error";
        }

        // Trả về trang danh sách yêu thích
        return "page/userWishlist";
    }

    @PostMapping("/remove-favorite")
    public String removeFavorite(
        @RequestParam("hotelId") int hotelId, 
        @RequestParam(value = "detail", defaultValue = "false") boolean detail,
        @RequestParam(value = "dateRange", defaultValue = "") String dateRange,
        @RequestParam(value = "adults", defaultValue = "1") int adults,
        @RequestParam(value = "children", defaultValue = "0") int children,
        @RequestParam(value = "rooms", defaultValue = "1") int rooms,
        
        HttpSession session, RedirectAttributes redirectAttributes
    ) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            // Nếu người dùng chưa đăng nhập, chuyển hướng về trang đăng nhập
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }

        try {
            // Xoá mục yêu thích
            userWishlistRepository.deleteFavorite(sessionUser.getId(), hotelId);
            redirectAttributes.addFlashAttribute("successMessage", "Xoá thành công mục yêu thích.");
        } catch (Exception e) {
            // Xử lý lỗi khi xoá
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xoá mục yêu thích.");
        }

        // Chuyển hướng về trang danh sách yêu thích
        if(detail){
            String redirect = "";
            if(!"".equals(dateRange)) redirect += "&dateRange=" + URLEncoder.encode(dateRange, StandardCharsets.UTF_8);
            if(adults != 1) redirect += "&adults=" + adults;
            if(children != 0) redirect += "&children=" + children;
            if(rooms != 1) redirect += "&rooms=" + rooms;

            return "redirect:/hotel-detail?hotelId=" + hotelId + redirect;
        }
        return "redirect:/user-wishlist";
    }

    @PostMapping("/add-favorite")
    public String addFavorite(
        @RequestParam("hotelId") int hotelId, 
        @RequestParam(value = "detail", defaultValue = "false") boolean detail,
        @RequestParam(value = "dateRange", defaultValue = "") String dateRange,
        @RequestParam(value = "adults", defaultValue = "1") int adults,
        @RequestParam(value = "children", defaultValue = "0") int children,
        @RequestParam(value = "rooms", defaultValue = "1") int rooms,
        
        HttpSession session, RedirectAttributes redirectAttributes
    ) {
        // Lấy thông tin người dùng từ session
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }

        try {
            // Gọi repository để thêm khách sạn vào danh sách yêu thích
            userWishlistRepository.addFavorite(sessionUser.getId(), hotelId);
            redirectAttributes.addFlashAttribute("successMessage", "Khách sạn đã được thêm vào danh sách yêu thích.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xảy ra lỗi khi thêm vào danh sách yêu thích.");
        }

        // Chuyển hướng về trang chi tiết khách sạn
        if(detail){
            String redirect = "";
            if(!"".equals(dateRange)) redirect += "&dateRange=" + URLEncoder.encode(dateRange, StandardCharsets.UTF_8);
            if(adults != 1) redirect += "&adults=" + adults;
            if(children != 0) redirect += "&children=" + children;
            if(rooms != 1) redirect += "&rooms=" + rooms;

            return "redirect:/hotel-detail?hotelId=" + hotelId + redirect;
        }
        return "redirect:/user-wishlist";
    }

}
