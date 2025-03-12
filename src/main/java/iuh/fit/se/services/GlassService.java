package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.dtos.FilterRequest;
import iuh.fit.se.dtos.GlassDTO;
import iuh.fit.se.dtos.GlassStatistic;
import iuh.fit.se.dtos.GlassesDTO;
import iuh.fit.se.entities.Glass;

public interface GlassService {
	public List<Glass> findAll();
	
	public List<Glass> findByCategory(Long category_id);
	
	public List<Glass> findByCategoryAndGender(Long category_id, int gender);
	
	public List<GlassDTO> findByCategoryAndGenderAndFilter(Long category_id, boolean gender, FilterRequest filter);
	
	public Glass findById(Long id);
	
	public Glass save(Glass glass);
	
	public List<String> getAllBrand();
	
	public List<String> getAllShape();
	
	public List<String> getAllMaterial();
	
	public List<String> getAllColor();
	
	public List<Glass> search(String keyword);
	
	public List<GlassStatistic> getTop5Glasses();
	
	public GlassesDTO save(GlassesDTO glass);
	
	public boolean delete (long id);
	
	public GlassesDTO update(long id, GlassesDTO glass);
	
	public List<Glass> searchGlasses(String keyword);
}
