package iuh.fit.se.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.dtos.FilterRequest;
import iuh.fit.se.dtos.GlassDTO;
import iuh.fit.se.dtos.GlassStatistic;
import iuh.fit.se.dtos.GlassesDTO;
import iuh.fit.se.entities.Category;
import iuh.fit.se.entities.FrameSize;
import iuh.fit.se.entities.Glass;
import iuh.fit.se.entities.Specifications;
import iuh.fit.se.repositories.GlassRepositoty;
import iuh.fit.se.services.CategoryService;
import iuh.fit.se.services.FrameSizeService;
import iuh.fit.se.services.GlassService;
import iuh.fit.se.services.SpecificationService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class GlassServiceImpl implements GlassService{
	
	@Autowired
	GlassRepositoty glassRepository;
	
	@Autowired
	FrameSizeService frameSizeService;
	
	@Autowired
	SpecificationService specificationService;
	
	@Autowired
	CategoryService categoryService;

	@Autowired
	ModelMapper modelMapper;
	
	private  GlassesDTO convertToDTO(Glass glass) {
		GlassesDTO glassDTO= modelMapper.map(glass, GlassesDTO.class);
	    return glassDTO;
	}
	
	private Glass convertToEntity(GlassesDTO glassDTO) {
		Glass glass = modelMapper.map(glassDTO, Glass.class);
	    return glass;
	}
	
	
	@Override
	public List<Glass> findAll() {
		return glassRepository.findAll();
	}

	@Override
	public Glass findById(Long id) {
		return glassRepository.findById(id).orElse(null);
	}

	@Override
	public List<Glass> findByCategory(Long category_id) {
		return glassRepository.findByCategory(category_id);
	}

	@Override
	public List<Glass> findByCategoryAndGender(Long category_id, int gender) {
		return glassRepository.findByCategoryAndGender(category_id, gender);
	}
	
	@Override
	public Glass save(Glass glass) {
		return glassRepository.save(glass);
	}

	@Override
	public List<String> getAllBrand() {
		return glassRepository.getAllBrand();
	}

	@Override
	public List<String> getAllShape() {
		return glassRepository.getAllShape();
	}

	@Override
	public List<String> getAllMaterial() {
		return glassRepository.getAllMaterial();
	}

	@Override
	public List<String> getAllColor() {
		return glassRepository.getAllColor();
	}

	@Override
	public List<GlassDTO> findByCategoryAndGenderAndFilter(Long category_id, boolean gender, FilterRequest filter) {
		return glassRepository.findAll((root, query, criteriaBuilder) ->{
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.equal(root.get("category").get("id"), category_id));
	        predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
	        System.out.println(filter.getBrands());
	         
	        if(filter != null) {
	        	if (filter.getMinPrice() != null)
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
				if (filter.getMaxPrice() != null)
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
				if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
					List<String> brands = new ArrayList<>();
			        if(filter.getBrands().contains(",")) {
			        	brands = List.of(filter.getBrands().split(","));
			        	predicates.add(root.get("brand").in(brands));
			        }
			        else
			        	predicates.add(root.get("brand").in(filter.getBrands()));	
				}	
				if (filter.getShapes() != null && !filter.getShapes().isEmpty()) {
					List<String> shapes = new ArrayList<>();
					if (filter.getShapes().contains(",")) {
						shapes = List.of(filter.getShapes().split(","));
						predicates.add(root.get("specifications").get("shape").in(shapes));
					} else
						predicates.add(root.get("specifications").get("shape").in(filter.getShapes()));
				}
				if (filter.getMaterials() != null && !filter.getMaterials().isEmpty()) {
					List<String> materials = new ArrayList<>();
					if (filter.getMaterials().contains(",")) {
						materials = List.of(filter.getMaterials().split(","));
						predicates.add(root.get("specifications").get("material").in(materials));
					} else
						predicates.add(root.get("specifications").get("material").in(filter.getMaterials()));
				}
				if (filter.getColors() != null && !filter.getColors().isEmpty()) {
					List<String> colors = new ArrayList<>();
                    if (filter.getColors().contains(",")) {
                        colors = List.of(filter.getColors().split(","));
                        predicates.add(root.get("colorName").in(colors));
                    } else
                        predicates.add(root.get("colorName").in(filter.getColors()));
                }
            }
			query.multiselect(
					root.get("id"),
					root.get("imageSideUrl"),
					root.get("imageFrontUrl"),
					root.get("colorCode"),
					root.get("name"),
					root.get("brand"),
					root.get("price"));
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		}).stream().map(tuple -> new GlassDTO(
				tuple.getId(),
				tuple.getImageSideUrl(),
				tuple.getImageFrontUrl(),
				tuple.getColorCode(),
				tuple.getName(),
				tuple.getBrand(),
				tuple.getPrice())).collect(Collectors.toList());
	}

	@Override
	public List<Glass> search(String keyword) {
		return glassRepository.search(keyword);
	}

	@Override
	public List<GlassStatistic> getTop5Glasses() {
		List<Object[]> list = glassRepository.getTop5Glasses();
		return list.stream().map(item -> new GlassStatistic((Long)item[0], (String)item[1], (int)item[2])).collect(Collectors.toList());
	}
	@Override
	public boolean delete(long id) {

		// Check id exists or not	
		if (this.findById(id) == null) {
			return false;
		}
		// Check if the glass has order items
		if (this.findById(id).getOrderItems().isEmpty() == false) {
			return false;
		}
		glassRepository.deleteById(id);
		return true;
		
		
	}
	@Transactional
	@Override
	public GlassesDTO update(long id, GlassesDTO glassDTO) {
		if (this.findById(id) == null) {
			return null;
		}
		// Update
		Glass glass = this.convertToEntity(glassDTO);
		System.out.println(glass);
		Category category = categoryService.findById(glass.getCategory().getId());
		glass.setCategory(category);
		
		glassRepository.save(glass);
		return this.convertToDTO(glass);
	}

	@Override
	public List<Glass> searchGlasses(String keyword) {
		// TODO Auto-generated method stub
		return glassRepository.search(keyword);
	}
	
	@Transactional
	@Override
	public GlassesDTO save(GlassesDTO glassDTO) {
		Glass glass = this.convertToEntity(glassDTO);
		System.out.println(glass);
		frameSizeService.save(glass.getFrameSize());
		specificationService.save(glass.getSpecifications());
//		FrameSize frameSize = frameSizeService.findById(glass.getFrameSize().getId());
//		FrameSize frameSize1 = new FrameSize();
//		frameSize1.setBridge(frameSize.getBridge());
//		frameSize1.setFrameWeight(frameSize.getFrameWeight());
//		frameSize1.setFrameWidth(frameSize.getFrameWidth());
////		frameSize1.setGlass(frameSize.getGlass());
//		frameSize1.setLensHeight(frameSize.getLensHeight());
//		frameSize1.setLensWidth(frameSize.getLensWidth());
//		frameSize1.setTempleLength(frameSize.getTempleLength());
//		FrameSize fs = frameSizeService.save(frameSize1);
//		System.out.println(fs);
//		glass.setFrameSize(fs);
//		Specifications  specifications = specificationService.findById(glass.getSpecifications().getId());
//		Specifications specifications1 = new Specifications();
//		specifications1.setAvailableAsProgressiveBifocal(specifications.getAvailableAsProgressiveBifocal());
//		specifications1.setFeature(specifications.getFeature());
//		specifications1.setFrameSizeDescription(specifications.getFrameSizeDescription());
//		specifications1.setMaterial(specifications.getMaterial());
//		specifications1.setPdRange(specifications.getPdRange());
//		specifications1.setPrescriptionRange(specifications.getPrescriptionRange());
//		specifications1.setReaders(specifications.getReaders());
//		specifications1.setRim(specifications.getRim());
//		specifications1.setShape(specifications.getShape());
//		Specifications specs = specificationService.save(specifications1);
//		System.out.println(specs);
//		glass.setSpecifications(specs);
		Category category = categoryService.findById(glass.getCategory().getId());
		glass.setCategory(category);
		glass = glassRepository.save(glass);
//		fs.setGlass(glass);
//		specs.setGlass(glass);
		return this.convertToDTO(glass);
	}
	
}
