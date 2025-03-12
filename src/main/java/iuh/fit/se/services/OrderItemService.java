package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.dtos.OrderItemDTO;
import iuh.fit.se.entities.OrderItem;

public interface OrderItemService {
	
	public List<OrderItemDTO> findByOrderItemIdOrderId(Long orderId);
}
