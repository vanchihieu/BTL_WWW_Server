package iuh.fit.se.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);
	
	Optional<List<User>> findByRole(Role role);
}
