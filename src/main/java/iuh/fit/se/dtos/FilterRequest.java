package iuh.fit.se.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest {
	private String minPrice;
	private String maxPrice;
	private String brands;
	private String shapes;
	private String materials;
	private String colors;
	
}
