package iuh.fit.se.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.email.Email;
import iuh.fit.se.services.EmailService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/send-email")
public class EmailController {
	
	@Autowired
	private EmailService emailService;
	
	@PostMapping()
	public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody Email email) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		emailService.sendEmail(email);
		response.put("status", HttpStatus.CREATED.value());
		response.put("data", "Email send successfully");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
}
