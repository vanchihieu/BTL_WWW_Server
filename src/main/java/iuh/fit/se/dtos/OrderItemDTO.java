package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class OrderItemDTO {
	private String name;
	private String image_side_url;
	private String brand;
	private String color_name;
	private String color_code;
	private int quantity;
	private Double unit_price;
	private Double total_price;
	public OrderItemDTO(String name, String image_side_url, String brand, String color_name, String color_code,
			int quantity, Double unit_price, Double total_price) {
		super();
		this.name = name;
		this.image_side_url = image_side_url;
		this.brand = brand;
		this.color_name = color_name;
		this.color_code = color_code;
		this.quantity = quantity;
		this.unit_price = unit_price;
		this.total_price = total_price;
	}
	public OrderItemDTO() {
		super();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImage_side_url() {
		return image_side_url;
	}
	public void setImage_side_url(String image_side_url) {
		this.image_side_url = image_side_url;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getColor_name() {
		return color_name;
	}
	public void setColor_name(String color_name) {
		this.color_name = color_name;
	}
	public String getColor_code() {
		return color_code;
	}
	public void setColor_code(String color_code) {
		this.color_code = color_code;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Double getUnit_price() {
		return unit_price;
	}
	public void setUnit_price(Double unit_price) {
		this.unit_price = unit_price;
	}
	public Double getTotal_price() {
		return total_price;
	}
	public void setTotal_price(Double total_price) {
		this.total_price = total_price;
	}
	
	
}
