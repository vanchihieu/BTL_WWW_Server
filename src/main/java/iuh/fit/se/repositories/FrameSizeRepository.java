package iuh.fit.se.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import iuh.fit.se.entities.FrameSize;

@RepositoryRestResource(collectionResourceRel = "frameSize", path = "frameSize", exported = false)
public interface FrameSizeRepository extends JpaRepository<FrameSize, Long>{

}
