package org.swp391.hotelbookingsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.controller.chatbot.DialogflowWebhookController;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.repository.ReviewRepository;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;
    private DialogflowWebhookController dialogflowWebhookController;

    public ReviewService(DialogflowWebhookController dialogflowWebhookController) {
        this.dialogflowWebhookController = dialogflowWebhookController;
    }

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public double getAverageHotelRatingForHost(int hostId) {
        Double rating = reviewRepository.getAverageHotelRatingByHostId(hostId);
        return rating != null ? rating : 0.0;
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

    public List<Review> getTop5PublicPositiveReviews() {
        return reviewRepository.getTop5PublicPositiveReviews();
    }
}
