package iuh.fit.se.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import iuh.fit.se.entities.Specifications;

@RepositoryRestResource(collectionResourceRel = "specifications", path = "specifications")
public interface SpecificationRepository extends JpaRepository<Specifications, Long>{

}
