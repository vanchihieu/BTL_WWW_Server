package iuh.fit.se.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.lang.Collections;
import iuh.fit.se.entities.Order;
import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.UserRepository;
import iuh.fit.se.services.UserService;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncode;
	
	@Override
	public void updatePassword(String email, String newPassword) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Mã hóa mật khẩu mới
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
		
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public User findById(Long id) {
		return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Override
	public void changePassword(String username, String oldPassword, String newPassword) {
		Optional<User> user = userRepository.findByUsername(username);
		
		if (!passwordEncode.matches(oldPassword, user.get().getPassword())) {
			throw new RuntimeException("Mật khẩu cũ không đúng!");
		}
		
		user.get().setPassword(passwordEncode.encode(newPassword));
		userRepository.save(user.get());
	}

	@Override
	public User save(User user) {
		return userRepository.save(user);
	}
	@Override
	public List<User> findAll() {
		return this.userRepository.findAll();
	}
	
	@Override
	public void deleteById(Long id) {
	    if (!userRepository.existsById(id)) {
	        throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
	    }
		if (userRepository.findById(id).get().getOrders().size() > 0) {
			throw new RuntimeException("Không thể xóa người dùng đã đặt hàng");
		}
	    userRepository.deleteById(id);
	}
	@Override
	public User updateUser (User user) {
		User userInDB = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
		
		if (userInDB == null) {
			return null;
		} else {
			userInDB.setFullname(user.getFullname());
			userInDB.setEmail(user.getEmail());
			userInDB.setPhone(user.getPhone());
			userInDB.setAddress(user.getAddress());
			userInDB.setGender(user.isGender());
			userInDB.setDob(user.getDob());
			userInDB.setRole(user.getRole());
			userInDB.setUsername(user.getUsername());
			userRepository.save(userInDB);
			return userInDB;
		}
	}
	
	@Override
	public List<User> filterUsers(String keyword, String gender, String role) {
	    Specification<User> spec = Specification.where(null);

	    // Thêm điều kiện tìm kiếm theo từ khóa
	    if (keyword != null && !keyword.isEmpty()) {
	    	System.out.println(keyword);
	        spec = spec.and(containsKeyword(keyword));
	    }

	    // Thêm điều kiện lọc theo giới tính
	    if (gender != null && !gender.isEmpty()) {
	    	System.out.println(gender);    	
	        spec = spec.and(hasGender(gender.equals("Male")));
	    }

	    // Thêm điều kiện lọc theo vai trò
	    if (role != null && !role.isEmpty()) {
	    	System.out.println(role);
	    	if(role.equals("ADMIN")) {
	    		spec = spec.and(hasRole(Role.ADMIN));
	    	}else if(role.equals("USER")) {
        		spec = spec.and(hasRole(Role.USER));
	    	}else
	    		spec = spec.and(hasRole(Role.SUPER));
	    }

	 // Trả về danh sách rỗng nếu không có bản ghi nào
	    return userRepository.findAll(spec);
	}
	public static Specification<User> containsKeyword(String keyword) {
	    return (root, query, criteriaBuilder) -> {
	        String likePattern = "%" + keyword.toLowerCase() + "%";
	        return criteriaBuilder.or(
	            criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), likePattern), // Tìm theo tên
	            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),    // Tìm theo email
	            criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern),    // Tìm theo số điện thoại
	            criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),  // Tìm theo username
	            criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), likePattern)  // Tìm theo address
	        );
	    };
	}
	public static Specification<User> hasGender(Boolean gender) {
	    return (root, query, criteriaBuilder) -> 
	        criteriaBuilder.equal(root.get("gender"), gender);
	}
	
	public static Specification<User> hasRole(Role role) {
	    return (root, query, criteriaBuilder) -> 
	        criteriaBuilder.equal(root.get("role"), role);
	}

	@Override
	public List<User> findByRole(Role role) {
		return userRepository.findByRole(role).orElse(null);
	}


}
