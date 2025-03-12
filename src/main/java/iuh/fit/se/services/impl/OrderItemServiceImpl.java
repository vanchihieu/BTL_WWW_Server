package iuh.fit.se.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.dtos.OrderItemDTO;
import iuh.fit.se.entities.OrderItem;
import iuh.fit.se.repositories.OrderItemRepository;
import iuh.fit.se.services.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService{

	@Autowired
	private OrderItemRepository orderItemRepository;
	
	@Override
	public List<OrderItemDTO> findByOrderItemIdOrderId(Long orderId) {
		List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
		return orderItems.stream().map(orderItem -> new OrderItemDTO(
				orderItem.getProduct().getName(),
				orderItem.getProduct().getImageSideUrl(),
				orderItem.getProduct().getBrand(),
				orderItem.getProduct().getColorName(),
				orderItem.getProduct().getColorCode(),
				orderItem.getQuantity(),
				orderItem.getUnitPrice(),
				orderItem.getTotalPrice())).toList();
	}
}
