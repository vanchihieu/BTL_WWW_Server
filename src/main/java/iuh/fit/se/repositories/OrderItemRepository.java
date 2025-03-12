package iuh.fit.se.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import iuh.fit.se.entities.OrderItem;
import iuh.fit.se.entities.OrderItemId;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
	
	
	public List<OrderItem> findByOrder_Id(Long orderId);
}
