package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.Reply;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.model.Hotel;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.service.ReviewService;
import org.swp391.hotelbookingsystem.service.HotelService;
import org.swp391.hotelbookingsystem.service.UserService;
import org.swp391.hotelbookingsystem.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ReplyController {
    private final ReviewService reviewService;
    private final HotelService hotelService;
    private final UserService userService;
    private final NotificationService notificationService;

    public ReplyController(ReviewService reviewService, HotelService hotelService,
                         UserService userService, NotificationService notificationService) {
        this.reviewService = reviewService;
        this.hotelService = hotelService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @PostMapping("/addReply")
    public Reply addReply(
        @RequestParam("reviewId") int reviewId,
        @RequestParam("userId") int userId,
        @RequestParam("comment") String comment
    ){
        Reply requestReview = Reply.builder()
            .reviewId(reviewId)
            .replierId(userId)
            .comment(comment)
            .build();

        int replyId = reviewService.addReply(requestReview);
        if(replyId == 0) return null;

        // Send notification to the original reviewer
        try {
            Review originalReview = reviewService.getReviewById(reviewId);
            User replier = userService.findUserById(userId);
            if (originalReview != null && replier != null) {
                Hotel hotel = hotelService.getHotelById(originalReview.getHotelId());
                if (hotel != null) {
                    notificationService.notifyReviewReply(
                        originalReview.getReviewerId(),
                        replier.getFullName(),
                        hotel.getHotelName(),
                        hotel.getHotelId()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending reply notification: " + e.getMessage());
        }

        return reviewService.getReplyById(replyId);
    }

    @PostMapping("/editReply")
    public Reply editReply(
        @RequestParam("replyId") int replyId,
        @RequestParam("comment") String comment
    ){
        if(comment.isBlank()) return null;
        
        int id = reviewService.editReply(replyId, comment);
        if(id == 0) return null;
        return reviewService.getReplyById(id);
    }

    @PostMapping("/deleteReply")
    public int deleteReply(@RequestParam("replyId") int replyId){
        return reviewService.deleteReply(replyId);
    }
}
