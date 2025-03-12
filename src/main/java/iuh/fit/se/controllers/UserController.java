package iuh.fit.se.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.dtos.UserDTO;
import iuh.fit.se.dtos.UserProfileDTO;
import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
	 @Autowired
	 private UserService userService;
	 
	 @Autowired
	 private PasswordEncoder passwordEncoder;
	 
	 	@GetMapping("/{username}")
		public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String username) {
	 		System.out.println("username: " + username);
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("status", HttpStatus.OK.value());
			response.put("data", userService.findByUsername(username));
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	 	
	 	@PostMapping("/change-password")
		public ResponseEntity<Map<String, Object>> handleChangePassword(@RequestBody Map<String, String> body) {
	 		try {
	 			String username = body.get("username");
	     		String oldPassword = body.get("oldPassword");
	     		String newPassword = body.get("newPassword");
	     		userService.changePassword(username, oldPassword, newPassword);
	     		
	     		Map<String, Object> response = new LinkedHashMap<String, Object>();
	     		response.put("status", HttpStatus.OK.value());
	     		response.put("message", "Đổi mật khẩu thành công!");
	     		return ResponseEntity.status(HttpStatus.OK).body(response);
			} catch (RuntimeException e) {
				Map<String, Object> response = new LinkedHashMap<String, Object>();
				response.put("status", HttpStatus.BAD_REQUEST.value());
				response.put("message", e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
     		
		}
	 	
	 	@PostMapping("/update-profile")
	 	public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UserProfileDTO userProfileDTO, BindingResult bindingResult) {
	 		
	 		if(bindingResult.hasErrors()) {
	 			Map<String, Object> errors = new LinkedHashMap<String, Object>();
	 			bindingResult.getFieldErrors().stream().forEach(result -> {
					errors.put(result.getField(), result.getDefaultMessage());
				});
	 			Map<String, Object> response = new LinkedHashMap<String, Object>();
	 			response.put("status", HttpStatus.BAD_REQUEST.value());
				response.put("errors", errors);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	 		}else {
	 			Optional<User> OPuser = userService.findByUsername(userProfileDTO.getUsername());
		 		User user = OPuser.get();
		 		user.setFullname(userProfileDTO.getFullname());
		 		user.setEmail(userProfileDTO.getEmail());
		 		user.setPhone(userProfileDTO.getPhone());
		 		user.setAddress(userProfileDTO.getAddress());
		 		user.setGender(userProfileDTO.isGender());
		 		user.setDob(userProfileDTO.getDob());
		 		userService.save(user);
		 		Map<String, Object> response = new LinkedHashMap<String, Object>();
		 		response.put("status", HttpStatus.OK.value());
		 		response.put("message", "Cập nhật thông tin thành công!");
		 		return ResponseEntity.status(HttpStatus.OK).body(response);
	 		}
	 		
	 	}
		@GetMapping("/all")
	    public ResponseEntity<Map<String, Object>> getAllUsers() {
	        try {  	
	            List<User> users = userService.findAll();
	            Map<String, Object> response = new LinkedHashMap<>();
	            response.put("status", HttpStatus.OK.value());
	            response.put("data", users);
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        } catch (Exception e) {
	            Map<String, Object> response = new LinkedHashMap<>();
	            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	            response.put("message", "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }
	    }
		
		@GetMapping("/all-by-role")
		public ResponseEntity<Map<String, Object>> getUsers(@AuthenticationPrincipal UserDetails userDetails){
			User currentUser = userService.findByUsername(userDetails.getUsername()).get();
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			List<User> users = null ;
			if(currentUser.getRole().equals(Role.SUPER)) {
                users = userService.findAll();
                response.put("status", HttpStatus.OK.value());
                response.put("data", users);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
            	users = userService.findByRole(Role.USER);
                users.add(currentUser);
                response.put("status", HttpStatus.OK.value());
                response.put("data", users);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
		}
	 	
	 	@DeleteMapping("/{id}")
	 	public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
	 	    try {
	 	        userService.deleteById(id);
	 	        Map<String, Object> response = new LinkedHashMap<>();
	 	        response.put("status", HttpStatus.OK.value());
	 	        response.put("message", "Xóa người dùng thành công!");
	 	        response.put("data", "Xóa người dùng thành công!");
	 	        return ResponseEntity.status(HttpStatus.OK).body(response);
	 	    } catch (Exception e) {
	 	        Map<String, Object> response = new LinkedHashMap<>();
	 	        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	 	        response.put("message", "Lỗi khi xóa người dùng: " + e.getMessage());
	 	        response.put("data", null);
	 	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	 	    }
	 	}
	 	@PostMapping("/add")
	 	public ResponseEntity<Map<String, Object>> addUser (@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
	 	    try {
	 	    	Map<String, Object> response = new LinkedHashMap<>();
				if (bindingResult.hasErrors()) {
					Map<String, Object> errors = new LinkedHashMap<String, Object>();
					
					bindingResult.getFieldErrors().stream().forEach(result -> {
						errors.put(result.getField(), result.getDefaultMessage());
					});
					response.put("status", HttpStatus.BAD_REQUEST.value());
					response.put("message", "Invalid data");
					response.put("errors", errors);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
				}
				else {
					User user = new User();
					user.setUsername(userDTO.getUsername());
					user.setEmail(userDTO.getEmail());
					user.setFullname(userDTO.getFullname());
					user.setPhone(userDTO.getPhone());
					user.setAddress(userDTO.getAddress());
					user.setGender(userDTO.isGender());
					user.setDob(userDTO.getDob());
					user.setRole(userDTO.getRole());
					user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
			 	    User savedUser = userService.save(user);
			 	    
			 	    response.put("status", HttpStatus.OK.value());
			 	    response.put("message", "User added successfully");
			 	    response.put("data", savedUser);
			 	    return ResponseEntity.status(HttpStatus.OK).body(response);
				}    	
	 	    } catch (Exception e) {
	 	        Map<String, Object> response = new LinkedHashMap<>();
	 	        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	 	        response.put("message", "Error adding user: " + e.getMessage());
	 	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	 	    }
	 	}
	 	
	 	
	 	@PutMapping("/update/{id}")
	 	public ResponseEntity<Map<String, Object>> updateUser (
	 			@PathVariable("id") Long id, 
	 			@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult
	 	) {
	 	    try {
	 	    	Map<String, Object> response = new LinkedHashMap<>();
	 	    	if(bindingResult.hasErrors()) {
	 	    		Map<String, Object> errors = new LinkedHashMap<String, Object>();
					
					bindingResult.getFieldErrors().stream().forEach(result -> {
						errors.put(result.getField(), result.getDefaultMessage());
					});
					System.out.println("errors: " + errors);
					response.put("status", HttpStatus.BAD_REQUEST.value());
					response.put("message", "Invalid data");
					response.put("errors", errors);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	 	    	}
	 	    	else {
	 	    		User existingUser = userService.findById(id);
	 	    		System.out.println("existingUser: " + existingUser);
		 	        if (existingUser == null) {
		 	            return ResponseEntity.notFound().build();
		 	        }

		 	        existingUser.setUsername(userDTO.getUsername());
		 	        existingUser.setEmail(userDTO.getEmail());
		 	        existingUser.setFullname(userDTO.getFullname());
		 	        existingUser.setPhone(userDTO.getPhone());
		 	        existingUser.setAddress(userDTO.getAddress());
		 	        existingUser.setGender(userDTO.isGender());
		 	        existingUser.setDob(userDTO.getDob());

		 	        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
		 	            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword())); 
		 	        }

		 	        userService.save(existingUser);  
		 	        response.put("status", HttpStatus.OK.value());
		 	        response.put("message", "User updated successfully"); 
		 	        response.put("data", existingUser);
		 	        return ResponseEntity.ok(response); 
	 	    	}
	 	        

	 	    } catch (Exception e) {
	 	        e.printStackTrace();
	 	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	 	    }
	 	}
	 	
	 	
	 	@GetMapping("/user/{id}")
		public ResponseEntity<Map<String, Object>> getUserById (@PathVariable Long id) {
			try {
				User user = userService.findById(id);

				if (user == null) {
					Map<String, Object> response = new LinkedHashMap<>();
		            response.put("status", HttpStatus.NOT_FOUND.value());
		            response.put("message", "User not found");
		            response.put("data", null);        
		            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
				}

				Map<String, Object> response = new LinkedHashMap<>();
				response.put("status", HttpStatus.OK.value());
				response.put("message", "Success");
				response.put("data", user);
				return ResponseEntity.ok(response);

			} catch (Exception e) {
				e.printStackTrace();
				Map<String, Object> response = new LinkedHashMap<>();
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
				response.put("message", "Error getting user: " + e.getMessage());
				response.put("data", null);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
	 	
	 	@GetMapping("/filter")
	 	public ResponseEntity<Map<String, Object>> getUsers(
	 		    @RequestParam(value = "keyword",required = false) String keyword,
	 		    @RequestParam(value = "gender",required = false) String gender,
	 		    @RequestParam(value = "role",required = false) String role
	 		) {
	 			System.out.println("keyword: " + keyword);
	 			System.out.println("gender: " + gender);
	 			System.out.println("role: " + role);
	 		   List<User> users = userService.filterUsers(keyword, gender, role);
	 		   Map<String, Object> response = new LinkedHashMap<String, Object>();
	 		   response.put("status", HttpStatus.OK.value());
	 		   response.put("data", users);
	 		   return ResponseEntity.status(HttpStatus.OK).body(response);
	 		}
	
}
