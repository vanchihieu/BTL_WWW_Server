package iuh.fit.se.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.services.SpecificationService;

@RestController 
@RequestMapping("/api/Specification")
public class SpecificationController {
	@Autowired
	SpecificationService specificationService;
	
	@GetMapping("/Specifications")
	public ResponseEntity<Map<String, Object>> getAll(){
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", specificationService.findAll());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/Specifications/{id}")
	public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id){
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", specificationService.findById(id));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
