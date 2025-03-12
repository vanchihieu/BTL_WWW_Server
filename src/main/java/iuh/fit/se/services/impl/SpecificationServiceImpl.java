package iuh.fit.se.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.entities.Specifications;
import iuh.fit.se.repositories.SpecificationRepository;
import iuh.fit.se.services.SpecificationService;

@Service
public class SpecificationServiceImpl implements SpecificationService{
	@Autowired
	SpecificationRepository specificationRepository;
	@Override
	public Specifications findById(Long id) {
		
		return specificationRepository.findById(id).orElse(null) ;
	}

	@Override
	public List<Specifications> findAll() {
		// TODO Auto-generated method stub
		return specificationRepository.findAll();
	}

	@Override
	public Specifications save(Specifications specifications) {
		return specificationRepository.save(specifications);
	}
	
}
