package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.ReviewService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class ReviewController {

    private final ReviewService reviewService;

    ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/review")
    public Review addReview(
        @RequestParam("hotelId") int hotelId,
        @RequestParam("rating") int rating,
        @RequestParam("comment") String comment,

        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");

        Review requestReview = Review.builder()
            .hotelId(hotelId)
            .reviewerId(user.getId())
            .rating(rating)
            .comment(comment)
            .build();

        int reviewId = reviewService.addReview(requestReview);
        if(reviewId == 0) return null;
        Review responseReview = reviewService.getReviewById(reviewId);

        if(comment.isBlank()) return null;
        return responseReview;
    }
}
