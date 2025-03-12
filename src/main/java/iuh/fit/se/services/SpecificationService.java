package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.entities.Specifications;

public interface SpecificationService {
	public Specifications findById(Long id);
	public List<Specifications> findAll();
	public Specifications save(Specifications specifications);
}
