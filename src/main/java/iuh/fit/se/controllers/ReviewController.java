package iuh.fit.se.controllers;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.dtos.ReviewRequest;
import iuh.fit.se.entities.Glass;
import iuh.fit.se.entities.Review;
import iuh.fit.se.entities.ReviewId;
import iuh.fit.se.entities.User;
import iuh.fit.se.services.GlassService;
import iuh.fit.se.services.ReviewService;
import iuh.fit.se.services.UserService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private GlassService glassService;
	
	@PostMapping("/create")
	public ResponseEntity<Map<String, Object>> createReview(@RequestBody ReviewRequest reviewRequest) {
		Review review = new Review();
		User user = userService.findByUsername(reviewRequest.getUsername()).get();
		Glass glass = glassService.findById(reviewRequest.getProductId());
		review.setUser(user);
		review.setProduct(glass);
		review.setRating(reviewRequest.getRating());
		review.setContent(reviewRequest.getContent());
		review.setCreatedAt(LocalDateTime.now());
		ReviewId reviewId = new ReviewId();
		reviewId.setProductId(reviewRequest.getProductId());
		reviewId.setUserId(user.getId());
		review.setReviewId(reviewId);
		reviewService.createReview(review);
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("message", "Tạo review thành công!");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> getReviewByProduct(@PathVariable Long productId,@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", reviewService.getReviewsByProductId(productId, page, size).getContent());
		response.put("message", !reviewService.getReviewsByProductId(productId, page, size).isLast());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
