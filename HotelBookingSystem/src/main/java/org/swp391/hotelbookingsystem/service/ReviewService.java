package org.swp391.hotelbookingsystem.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.controller.chatbot.DialogflowWebhookController;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {

    private final DialogflowWebhookController dialogflowWebhookController;
    @Autowired
    private ReviewRepository reviewRepository;

    ReviewService(DialogflowWebhookController dialogflowWebhookController) {
        this.dialogflowWebhookController = dialogflowWebhookController;
    }

    public List<Review> getTop5PublicPositiveReviews() {
        return reviewRepository.getTop5PublicPositiveReviews();
    }

    public List<Review> getRecentPublicReviews() {
        return reviewRepository.getRecentPublicReviews();
    }

    public boolean checkReview(int hotelId, int userId){
        return reviewRepository.checkReview(hotelId, userId);
    }

    public int addReview(Review review){
        if(reviewRepository.checkReview(review.getHotelId(), review.getReviewerId())) 
            return reviewRepository.addReview(review);
        return 0;        
    }

    public Review getReviewById(int id){
        return reviewRepository.getReviewById(id);
    }

    public List<Review> getHotelReview(int hotelId){
        return reviewRepository.getHotelReview(hotelId);
    }
}
