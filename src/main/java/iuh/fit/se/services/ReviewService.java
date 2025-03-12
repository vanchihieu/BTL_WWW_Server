package iuh.fit.se.services;

import org.springframework.data.domain.Page;

import iuh.fit.se.entities.Review;

public interface ReviewService {
	public Review createReview(Review review);
	
	public Page<Review> getReviewsByProductId(Long productId, int page, int size);
}
