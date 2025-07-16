package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.service.ReviewService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @PostMapping("/review")
    public Review addReview(
        @RequestParam("hotelId") int hotelId,
        @RequestParam("userId") int userId,
        @RequestParam("rating") int rating,
        @RequestParam("comment") String comment
    ) {
        Review requestReview = Review.builder()
            .hotelId(hotelId)
            .reviewerId(userId)
            .rating(rating)
            .comment(comment)
            .build();

        int reviewId = reviewService.review(requestReview);
        if(reviewId == 0) return null;
        return reviewService.getReviewById(reviewId);
    }

    @PostMapping("/deleteReview")
    public int deleteReview(
        @RequestParam("hotelId") int hotelId,
        @RequestParam("userId") int userId
    ) {
        return reviewService.deleteReview(hotelId, userId);
    }
}
