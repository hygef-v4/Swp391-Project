package org.swp391.hotelbookingsystem.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Reply;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.repository.ReviewRepository;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;

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

    public Review getReview(int hotelId, int userId){
        if(reviewRepository.checkReview(hotelId, userId))
            return reviewRepository.getReview(hotelId, userId);
        else return null;
    }

    public int review(Review review){
        if(!reviewRepository.checkReview(review.getHotelId(), review.getReviewerId())) 
            return reviewRepository.addReview(review);
        return reviewRepository.editReview(review);
    }

    public int deleteReview(int hotelId, int userId){
        return reviewRepository.deleteReview(hotelId, userId);
    }

    public Review getReviewById(int id){
        return reviewRepository.getReviewById(id);
    }

    public List<Review> getHotelReview(int hotelId){
        List<Review> reviews = reviewRepository.getHotelReview(hotelId);
        for (Review r : reviews) {
            List<Reply> replies = reviewRepository.getReviewReply(r.getReviewId());
            r.setReplies(replies);
        }return reviews;
    }

    public int addReply(Reply reply){
        return reviewRepository.addReply(reply);
    }

    public int editReply(int replyId, String comment){
        return reviewRepository.editReply(replyId, comment);
    }

    public int deleteReply(int replyId){
        return reviewRepository.deleteReply(replyId);
    }

    public Reply getReplyById(int id){
        return reviewRepository.getReplyById(id);
    }

    public List<Review> getTop5PublicPositiveReviews() {
        return reviewRepository.getTop5PublicPositiveReviews();
    }

    public int getTotalReviewCount() {
        return reviewRepository.countAllReviews();
    }

    public int getTotalReviewCountThisYear() {
        return reviewRepository.countReviewsThisYear();
    }

    public int getTotalReviewCountLastYear() {
        return reviewRepository.countReviewsLastYear();
    }


    public double getAverageRatingThisYear() {
        return reviewRepository.getAverageRatingThisYear();
    }


    public List<Integer> getRatingDistribution() {
        return reviewRepository.getRatingDistribution();
    }

    public void restoreReviewById(int id) {
        reviewRepository.setPublicStatus(id, true);
    }

    public void softDeleteReviewById(int id) {
        reviewRepository.setPublicStatus(id, false);
    }

    public List<Review> getAllPublicReviews() {
        return reviewRepository.getAllPublicReviews();
    }

    public List<Review> getReviewsByStatus(String status) {
        return reviewRepository.getReviewsByStatus(status);
    }

    public int getTotalReviewsForHost(int hostId) {
        return reviewRepository.countTotalReviewsByHostId(hostId);
    }

    public int getUnaddressedReviewsForHost(int hostId) {
        return reviewRepository.countUnaddressedReviewsByHostId(hostId);
    }

    public int getRecentReviewsForHost(int hostId) {
        return reviewRepository.countRecentReviewsByHostId(hostId);
    }

    public List<Review> getAllReviewsByHostId(int hostId) {
        return reviewRepository.getReviewsByHostId(hostId);
    }

}
