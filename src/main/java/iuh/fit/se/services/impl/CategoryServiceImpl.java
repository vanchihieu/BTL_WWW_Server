package iuh.fit.se.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.entities.Category;
import iuh.fit.se.repositories.CategoryRepository;
import iuh.fit.se.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Category findById(Long id) {
		return categoryRepository.findById(id).orElse(null);
	}

	@Override
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

}
