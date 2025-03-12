package iuh.fit.se.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import iuh.fit.se.entities.Review;
import iuh.fit.se.repositories.ReviewRepository;
import iuh.fit.se.services.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService{
	
	@Autowired
	private ReviewRepository  reviewRepository;
	
	@Override
	public Review createReview(Review review) {
		return reviewRepository.save(review);
	}

	@Override
	public Page<Review> getReviewsByProductId(Long productId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return reviewRepository.findByProductId(productId, pageable);
	}

}
