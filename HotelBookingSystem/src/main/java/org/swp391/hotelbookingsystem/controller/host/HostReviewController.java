package org.swp391.hotelbookingsystem.controller.host;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.Reply;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.ReviewService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HostReviewController {

    private final ReviewService reviewService;
    private final HotelService hotelService;

    public HostReviewController(ReviewService reviewService, HotelService hotelService) {
        this.reviewService = reviewService;
        this.hotelService = hotelService;
    }

    @GetMapping("/host-reviews")
    public String hostReview(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "rating", required = false) String rating,
                           @RequestParam(value = "hotelId", required = false) String hotelId,
                           @RequestParam(value = "replyStatus", required = false) String replyStatus,
                           @RequestParam(value = "sortBy", defaultValue = "newest") String sortBy,
                           HttpSession session, Model model) {
        User host = (User) session.getAttribute("user");
        int hostId = host.getId();

        // Add statistics
        model.addAttribute("averageRating", reviewService.getAverageHotelRatingForHost(hostId));
        model.addAttribute("totalReviews", reviewService.getTotalReviewsForHost(hostId));
        model.addAttribute("unaddressedReviews", reviewService.getUnaddressedReviewsForHost(hostId));
        model.addAttribute("recentReviews", reviewService.getRecentReviewsForHost(hostId));

        // Get host's hotels for filter dropdown
        List<Hotel> hostHotels = hotelService.getHotelsByHostId(hostId);
        model.addAttribute("hotels", hostHotels);

        // Load all reviews for this host
        List<Review> allReviews = reviewService.getAllReviewsByHostId(hostId);

        // Apply filters
        List<Review> filteredReviews = applyFilters(allReviews, rating, hotelId, replyStatus, sortBy);

        // Manual pagination
        int pageSize = 4;
        int totalFilteredReviews = filteredReviews.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalFilteredReviews);

        List<Review> currentReviews = (startIndex < totalFilteredReviews)
                ? filteredReviews.subList(startIndex, endIndex)
                : List.of();

        // Pagination attributes
        model.addAttribute("reviews", currentReviews);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalFilteredReviews / pageSize));
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalHotels", totalFilteredReviews); // reuse same var for consistency

        return "host/host-reviews";
    }

    // Delete reply endpoint for host
    @PostMapping("/host/deleteReply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReply(@RequestParam("replyId") int replyId, HttpSession session) {
        try {
            // Check if user is authenticated
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Bạn cần đăng nhập để thực hiện hành động này."));
            }

            // Get the reply to check ownership
            Reply reply = reviewService.getReplyById(replyId);
            if (reply == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy phản hồi."));
            }

            // Check if the user owns this reply
            if (reply.getReplierId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Bạn chỉ có thể xóa phản hồi của chính mình."));
            }

            // Delete the reply
            int deletedReplyId = reviewService.deleteReply(replyId);
            if (deletedReplyId > 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phản hồi đã được xóa thành công.",
                    "deletedReplyId", deletedReplyId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Có lỗi xảy ra khi xóa phản hồi."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // Apply filters to reviews
    private List<Review> applyFilters(List<Review> reviews, String rating, String hotelId, String replyStatus, String sortBy) {
        return reviews.stream()
                .filter(review -> {
                    // Rating filter
                    if (rating != null && !rating.isEmpty()) {
                        try {
                            int ratingValue = Integer.parseInt(rating);
                            if (review.getRating() != ratingValue) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Invalid rating, skip filter
                        }
                    }

                    // Hotel filter
                    if (hotelId != null && !hotelId.isEmpty()) {
                        try {
                            int hotelIdValue = Integer.parseInt(hotelId);
                            if (review.getHotelId() != hotelIdValue) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Invalid hotel ID, skip filter
                        }
                    }

                    // Reply status filter
                    if (replyStatus != null && !replyStatus.isEmpty()) {
                        boolean hasReplies = review.getReplies() != null && !review.getReplies().isEmpty();
                        if ("replied".equals(replyStatus) && !hasReplies) {
                            return false;
                        }
                        if ("not_replied".equals(replyStatus) && hasReplies) {
                            return false;
                        }
                    }

                    return true;
                })
                .sorted(getSortComparator(sortBy))
                .collect(Collectors.toList());
    }

    // Get sort comparator
    private Comparator<Review> getSortComparator(String sortBy) {
        return switch (sortBy) {
            case "oldest" -> Comparator.comparing(Review::getCreatedAt);
            case "highest_rating" -> Comparator.comparing(Review::getRating).reversed();
            case "lowest_rating" -> Comparator.comparing(Review::getRating);
            default -> Comparator.comparing(Review::getCreatedAt).reversed(); // newest first
        };
    }
}
