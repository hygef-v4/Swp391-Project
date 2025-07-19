package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.NotificationService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ReviewController {
    private final ReviewService reviewService;
    private final HotelService hotelService;
    private final UserService userService;
    private final NotificationService notificationService;

    public ReviewController(ReviewService reviewService, HotelService hotelService,
                          UserService userService, NotificationService notificationService){
        this.reviewService = reviewService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.notificationService = notificationService;
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

        // Send notification to hotel owner
        try {
            Hotel hotel = hotelService.getHotelById(hotelId);
            User reviewer = userService.findUserById(userId);
            if (hotel != null && reviewer != null) {
                notificationService.notifyNewReview(
                    hotel.getHostId(),
                    reviewer.getFullName(),
                    hotel.getHotelName(),
                    rating,
                    hotelId
                );
            }
        } catch (Exception e) {
            System.err.println("Error sending review notification: " + e.getMessage());
        }

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
