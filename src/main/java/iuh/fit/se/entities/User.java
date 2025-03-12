package iuh.fit.se.entities;

import java.sql.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fullname;

	private String username;

	private String password;

	private String email;

	private Date dob;

	private String phone;

	private String address;
	private boolean gender;
	@Enumerated(EnumType.STRING)
	private Role role;
	@OneToMany(mappedBy = "modifyProductId.modifiedBy")
	@JsonIgnore
	private List<ModifyProduct> modifyProducts;
	
	@OneToMany(mappedBy = "user")
	@JsonIgnore
	public List<Order> orders;
	
	@OneToMany(mappedBy = "reviewId.userId")
	@JsonIgnore
	private List<Review> reviews;
	
	public User(Long id, String fullname, String username, String password, String email, Date dob, String phone,
			String address, boolean gender, Role role) {
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
	public User() {
		super();
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id);
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	
	public List<Review> getReviews() {
		return reviews;
	}
	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", fullname=" + fullname + ", username=" + username + ", password=" + password
				+ ", email=" + email + ", dob=" + dob + ", phone=" + phone + ", address=" + address + ", gender="
				+ gender + ", role=" + role + "]";
	}
	
	
	
	
}
