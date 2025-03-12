package iuh.fit.se.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.entities.Notification;
import iuh.fit.se.services.NotificationService;


@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationController {
	
	@Autowired
    private NotificationService notificationService;
	
	 // Lấy danh sách thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
    	System.out.println("getUnreadNotifications");
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Notification> notifications = notificationService.findByIsReadFalse();
		response.put("status", HttpStatus.OK.value());
		response.put("data", notifications);
		return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
 // Đánh dấu thông báo là đã đọc
    @PostMapping("/{id}/mark-as-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
    	Map<String, Object> response = new LinkedHashMap<String, Object>();
    	System.out.println("markAsRead");
        Notification notification = notificationService.findById(id);
        notification.setRead(true);
        notificationService.save(notification);
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Notification has been marked as read");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
