package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
	private Long productId;
	private String username;
	
	private int rating;
	private String content;
}
