package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.entities.Notification;

public interface NotificationService {
	
	public Notification save(Notification notification);
	public Notification findById(Long id);
	public List<Notification> findByIsReadFalse();
}
