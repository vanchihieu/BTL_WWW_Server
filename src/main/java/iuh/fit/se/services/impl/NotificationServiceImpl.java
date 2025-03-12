package iuh.fit.se.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.entities.Notification;
import iuh.fit.se.repositories.NotificationRepository;
import iuh.fit.se.services.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService{

	@Autowired
	NotificationRepository notificationRepository;
	
	@Override
	public List<Notification> findByIsReadFalse() {
		return notificationRepository.findByIsReadFalse();
	}

	@Override
	public Notification findById(Long id) {
		return notificationRepository.findById(id).orElse(null);
	}
	
	@Override
	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
		
	}

	
}
