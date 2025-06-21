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
}
