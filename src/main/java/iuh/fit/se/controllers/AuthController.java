package iuh.fit.se.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.auth.AuthRequest;
import iuh.fit.se.auth.AuthResponse;
import iuh.fit.se.auth.RegisterRequest;
import iuh.fit.se.configs.CustomUserDetails;
import iuh.fit.se.configs.JwtService;
import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.UserRepository;
import iuh.fit.se.services.UserService;
import iuh.fit.se.utils.JwtTokenUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request,
			BindingResult result) {
		// Check if username exists
		Map<String, Object> response = new LinkedHashMap<String, Object>();

		if (result.hasErrors()) {
			Map<String, Object> errors = new LinkedHashMap<>();
			result.getFieldErrors().forEach(error -> {
				errors.put(error.getField(), error.getDefaultMessage());
			});

			response.put("status", HttpStatus.BAD_REQUEST.value());
			response.put("errors", errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} else {
			if (userRepository.findByUsername(request.getUsername()).isPresent()) {
				response.put("status", HttpStatus.BAD_REQUEST.value());
				response.put("data", "Username already exists");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Create new user
			User user = new User();
			user.setUsername(request.getUsername());
			user.setFullname(request.getFullname());
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			user.setEmail(request.getEmail());
			user.setDob(request.getDob());
			user.setPhone(request.getPhone());
			user.setAddress(request.getAddress());
			user.setGender(request.isGender());

			if (request.getRole() != null && request.getRole() == Role.SUPER) {
				user.setRole(Role.SUPER);
			} else if (request.getRole() != null && request.getRole() == Role.ADMIN) {
				user.setRole(Role.ADMIN);
			} else {
				user.setRole(Role.USER); // Default role
			}

			userRepository.save(user);

			response.put("status", HttpStatus.OK.value());
			response.put("data", "Đăng ký tài khoản thành công");

			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {
		try {
			@SuppressWarnings("unused")
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

			String token = jwtService.generateToken(new CustomUserDetails(user));
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("status", HttpStatus.OK.value());
			response.put("data",
					new AuthResponse(token, user.getUsername(), user.getFullname(), user.getRole().toString()));
			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (BadCredentialsException e) {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("status", HttpStatus.UNAUTHORIZED.value());
			response.put("message", "Invalid username or password");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		String email = request.get("email");

		// Tạo JWT token
		String token = jwtTokenUtil.generateResetToken(email);

		// Gửi email
		sendResetPasswordEmail(email, token);

		response.put("status", HttpStatus.OK.value());
		response.put("data", "Password reset link has been sent to your email.");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	private void sendResetPasswordEmail(String email, String token) {
		String resetUrl = "http://localhost:8081/reset-password?token=" + token;
		String subject = "Reset Your Password";
//        String message = "Click the link below to reset your password:\n" + resetUrl;
		// HTML Template for Email
		String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
				+ "<h2 style=\"color: #333;\">Password Reset Request</h2>"
				+ "<p>We received a request to reset your password. Click the link below to reset your password:</p>"
				+ "<a href=\"" + resetUrl
				+ "\" style=\"display: inline-block; padding: 10px 20px; margin: 20px 0; font-size: 16px; color: #fff; background-color: #007bff; text-decoration: none; border-radius: 5px;\">Reset Password</a>"
				+ "<p>If you did not request a password reset, please ignore this email or contact support if you have questions.</p>"
				+ "<p>Thank you,<br>Your Company Team</p>" + "</div>";

		MimeMessage messageMail = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(messageMail, true);
			helper.setFrom("sendingemaileventhub@gmail.com");
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);
			mailSender.send(messageMail);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		String token = request.get("token");
		String newPassword = request.get("newPassword");

		// Xác thực token
		if (!jwtTokenUtil.validateToken(token)) {
			response.put("status", HttpStatus.BAD_REQUEST.value());
			response.put("data", "Invalid or expired token.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		// Lấy email từ token
		String email = jwtTokenUtil.getEmailFromToken(token);

		// Cập nhật mật khẩu
		userService.updatePassword(email, newPassword);

		response.put("status", HttpStatus.OK.value());
		response.put("data", "Password reset successfully.");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
