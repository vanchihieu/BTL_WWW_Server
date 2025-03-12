package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.entities.Category;

public interface CategoryService {
	public Category findById(Long id);
	
	public List<Category> findAll();
}
