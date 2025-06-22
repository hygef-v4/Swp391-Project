package org.swp391.hotelbookingsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Review;
import org.swp391.hotelbookingsystem.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

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

    public List<Review> getTop5PublicPositiveReviews() {
        return reviewRepository.getTop5PublicPositiveReviews();
    }

    public List<Review> getReviewsByStatus(String status, int page, int size) {
        int offset = (page - 1) * size;
        return reviewRepository.getReviewsByStatus(status, offset, size);
    }

    public int getTotalReviewCount() {
        return reviewRepository.countAllReviews();
    }


    public double getAverageRating() {
        return reviewRepository.getAverageRating();
    }

    public List<Integer> getRatingDistribution() {
        return reviewRepository.getRatingDistribution();
    }

    public int getTotalPagesByStatus(String filter, int pageSize) {
        int totalReviews = switch (filter.toLowerCase()) {
            case "published" -> reviewRepository.countByPublic(true);
            case "deleted" -> reviewRepository.countByPublic(false);
            default -> reviewRepository.countAllReviews();
        };
        return (int) Math.ceil((double) totalReviews / pageSize);
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

}
