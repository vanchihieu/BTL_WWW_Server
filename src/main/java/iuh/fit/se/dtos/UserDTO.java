package iuh.fit.se.dtos;

import java.sql.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import iuh.fit.se.entities.ModifyProduct;
import iuh.fit.se.entities.Order;
import iuh.fit.se.entities.Review;
import iuh.fit.se.entities.Role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public class UserDTO {
	private Long id;
	
	@NotEmpty(message = "Họ tên không được để trống")
	private String fullname;
	@NotEmpty(message = "Tên đăng nhập không được để trống")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản không chứa ký tự đặc biệt")
	private String username;
	@NotEmpty(message = "Mật khẩu không được để trống")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$",
	message = "Mật khẩu phải chứa ít nhất 1 ký tự in hoa, 1 ký tự số, 1 ký tự đặc biệt và có ít nhất 8 ký tự")
	private String password;
	@NotEmpty(message = "Email không được để trống")
	@Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*", message = "Email không hợp lệ")
	private String email;
	@NotNull(message = "Ngày sinh không được để trống")
	@Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
	private Date dob;
	@NotEmpty(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ")
	private String phone;
	@NotEmpty(message = "Địa chỉ không được để trống")
	@Pattern(regexp = "^[a-zA-Z0-9\\s,.'-]{3,100}$", message = "Địa chỉ không hợp lệ")
	private String address;
	private boolean gender;

	private Role role;
	
	@JsonIgnore
	private List<ModifyProduct> modifyProducts;
	
	@JsonIgnore
	public List<Order> orders;
	
	@JsonIgnore
	private List<Review> reviews;

	public UserDTO(Long id, @NotEmpty(message = "Họ tên không được để trống") String fullname,
			@NotEmpty(message = "Tên đăng nhập không được để trống") @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản không chứa ký tự đặc biệt") String username,
			@NotEmpty(message = "Mật khẩu không được để trống") @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$", message = "Mật khẩu phải chứa ít nhất 1 ký tự in hoa, 1 ký tự số, 1 ký tự đặc biệt và có ít nhất 8 ký tự") String password,
			@NotEmpty(message = "Email không được để trống") @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*", message = "Email không hợp lệ") String email,
			@NotNull(message = "Ngày sinh không được để trống") Date dob,
			@NotEmpty(message = "Số điện thoại không được để trống") @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ") String phone,
			@NotEmpty(message = "Địa chỉ không được để trống") @Pattern(regexp = "^[a-zA-Z0-9\\s,.'-]{3,100}$", message = "Địa chỉ không hợp lệ") String address,
			boolean gender, Role role) {
		super();
		this.id = id;
		this.fullname = fullname;
		this.username = username;
		this.password = password;
		this.email = email;
		this.dob = dob;
		this.phone = phone;
		this.address = address;
		this.gender = gender;
		this.role = role;
	}

	public UserDTO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public List<ModifyProduct> getModifyProducts() {
		return modifyProducts;
	}

	public void setModifyProducts(List<ModifyProduct> modifyProducts) {
		this.modifyProducts = modifyProducts;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}
	
	
}
