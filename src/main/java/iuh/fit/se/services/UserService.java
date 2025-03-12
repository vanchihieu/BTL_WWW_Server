package iuh.fit.se.services;

import java.util.List;
import java.util.Optional;

import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;

public interface UserService {
	public void updatePassword(String email, String newPassword);
	
	public Optional<User> findByUsername(String username);
	
	public User findById(Long id);
	
	public void changePassword(String username, String oldPassword, String newPassword);
	
	public User save(User user);
	
	public List<User> findAll();
	
	public void deleteById(Long id);
	
	public User updateUser (User user);
	
	public List<User> filterUsers(String keyword, String gender, String role);
	
	public List<User> findByRole(Role role);
}
