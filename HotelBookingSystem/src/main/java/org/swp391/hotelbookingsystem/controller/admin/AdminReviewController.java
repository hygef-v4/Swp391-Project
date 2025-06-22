package org.swp391.hotelbookingsystem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/admin-review")
    public String showReviewList(@RequestParam(defaultValue = "all") String filter,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) String hotelName,
                                 Model model) {
        int pageSize = 5;

        // ✅ 1. Lấy toàn bộ reviews theo filter (KHÔNG phân trang)
        List<Review> allReviews = reviewService.getReviewsByStatus(filter);

        // ✅ 2. Lọc theo hotelName nếu có
        if (hotelName != null && !hotelName.isEmpty()) {
            allReviews = allReviews.stream()
                    .filter(r -> hotelName.equals(r.getHotelName()))
                    .toList();
        }

        // ✅ 3. Tính phân trang thủ công
        int totalReviews = allReviews.size();
        int totalPages = (int) Math.ceil((double) totalReviews / pageSize);
        int fromIndex = Math.max((page - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, totalReviews);
        List<Review> reviews = allReviews.subList(fromIndex, toIndex);

        // ✅ 4. Dữ liệu phân tích
        List<Integer> ratingDistribution = reviewService.getRatingDistribution();
        int totalPublicReviews = ratingDistribution.stream().mapToInt(Integer::intValue).sum();

        // ✅ 5. Danh sách khách sạn cho dropdown
        List<Review> allPublicReviews = reviewService.getAllPublicReviews();
        List<String> hotelNames = allPublicReviews.stream()
                .map(Review::getHotelName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .sorted()
                .toList();

        // ✅ 6. Đưa dữ liệu ra view
        model.addAttribute("reviewList", reviews);
        model.addAttribute("filter", filter);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalReviews", reviewService.getTotalReviewCount());
        model.addAttribute("avgRating", reviewService.getAverageRating());
        model.addAttribute("ratingDistribution", ratingDistribution);
        model.addAttribute("totalPublicReviews", totalPublicReviews);
        model.addAttribute("hotelNames", hotelNames);
        model.addAttribute("selectedHotelName", hotelName);

        return "admin/admin-review";
    }

    @GetMapping("/admin-review/restore/{id}")
    public String restoreReview(@PathVariable("id") int id,
                                @RequestParam(defaultValue = "all") String filter,
                                @RequestParam(defaultValue = "1") int page) {
        reviewService.restoreReviewById(id);
        return "redirect:/admin-review?filter=" + filter + "&page=" + page;
    }

    @GetMapping("/admin-review/delete/{id}")
    public String deleteReview(@PathVariable("id") int id,
                               @RequestParam(defaultValue = "all") String filter,
                               @RequestParam(defaultValue = "1") int page) {
        reviewService.softDeleteReviewById(id);
        return "redirect:/admin-review?filter=" + filter + "&page=" + page;
    }

}
