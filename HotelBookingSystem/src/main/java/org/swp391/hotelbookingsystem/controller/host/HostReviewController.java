package org.swp391.hotelbookingsystem.controller.host;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.ReviewService;

import java.util.List;

@Controller
public class HostReviewController {

    private final ReviewService reviewService;

    public HostReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/host-reviews")
    public String hostReview(@RequestParam(value = "page", defaultValue = "1") int page, HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");

        int hostId = host.getId();

        model.addAttribute("averageRating", reviewService.getAverageHotelRatingForHost(hostId));
        model.addAttribute("totalReviews", reviewService.getTotalReviewsForHost(hostId));
        model.addAttribute("unaddressedReviews", reviewService.getUnaddressedReviewsForHost(hostId));
        model.addAttribute("recentReviews", reviewService.getRecentReviewsForHost(hostId));

        // Load all reviews for this host
        List<Review> allReviews = reviewService.getAllReviewsByHostId(hostId);

        // Manual pagination
        int pageSize = 4;
        int totalReviews = allReviews.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalReviews);

        List<Review> currentReviews = (startIndex < totalReviews)
                ? allReviews.subList(startIndex, endIndex)
                : List.of();

        // Pagination attributes
        model.addAttribute("reviews", currentReviews);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalReviews / pageSize));
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalHotels", totalReviews); // reuse same var for consistency

        return "host/host-reviews";
    }
}
