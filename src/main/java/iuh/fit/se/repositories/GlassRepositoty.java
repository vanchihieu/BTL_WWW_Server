package iuh.fit.se.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import iuh.fit.se.dtos.GlassStatistic;
import iuh.fit.se.entities.Glass;

@RepositoryRestResource(collectionResourceRel = "glasses", path = "glasses")
public interface GlassRepositoty extends JpaRepository<Glass, Long>, JpaSpecificationExecutor<Glass>{
	
	@Query(value = "select * from glasses g where g.category_id = :category", nativeQuery = true)
	List<Glass> findByCategory(@Param("category") Long category_id);
	
	
	@Query(value = "select * from glasses g where category_id = :category and gender = :gender", nativeQuery = true) 
	List<Glass> findByCategoryAndGender(@Param("category") Long category_id, @Param("gender") int gender);
	
	@Query(value = "select DISTINCT brand from glasses", nativeQuery = true)
	List<String> getAllBrand();
	
	@Query(value = "select DISTINCT shape from glasses g join specifications s on g.specifications_id = s.id", nativeQuery = true)
	List<String> getAllShape();
	
	@Query(value = "select DISTINCT material from glasses g join specifications s on g.specifications_id = s.id", nativeQuery = true)
	List<String> getAllMaterial();
	
	@Query(value = "select DISTINCT color_name from glasses", nativeQuery = true)
	List<String> getAllColor();
	
	@Query("select g from Glass g where g.name like %:keyword% "
			+ "or g.brand like %:keyword% "
			+ "or g.colorName like %:keyword% "
			+ "or g.colorCode like %:keyword% "
			+ "or g.description like %:keyword% "
			+ "or g.specifications.shape like %:keyword% "
			+ "or g.specifications.material like %:keyword%")
	List<Glass> search(@Param("keyword") String keyword);
	
	@Query(value = "select TOP 5 g.id, g.name, sum(o.quantity) as count from glasses g join order_items o on g.id = o.product_id group by g.id,g.name order by sum(o.quantity) DESC", nativeQuery = true)
	List<Object[]> getTop5Glasses();
}
