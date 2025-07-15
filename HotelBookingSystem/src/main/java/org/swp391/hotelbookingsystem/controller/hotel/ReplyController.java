package org.swp391.hotelbookingsystem.controller.hotel;

import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swp391.hotelbookingsystem.model.Reply;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.service.ReviewService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ReplyController {
    private final ReviewService reviewService;

    public ReplyController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/addReply")
    public Reply addReply(
        @RequestParam("reviewId") int reviewId,
        @RequestParam("userId") int userId,
        @RequestParam("comment") String comment
    ){
        if(comment.isBlank()) return null;
        Reply requestReview = Reply.builder()
            .reviewId(reviewId)
            .replierId(userId)
            .comment(comment)
            .build();

        int replyId = reviewService.addReply(requestReview);
        if(replyId == 0) return null;
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
