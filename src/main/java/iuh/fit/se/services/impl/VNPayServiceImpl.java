package iuh.fit.se.services.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import iuh.fit.se.configs.VNPayConfig;
import iuh.fit.se.dtos.OrderItemRequest;
import iuh.fit.se.dtos.OrderRequest;
import iuh.fit.se.entities.Glass;
import iuh.fit.se.entities.Notification;
import iuh.fit.se.entities.Order;
import iuh.fit.se.entities.OrderItem;
import iuh.fit.se.entities.OrderItemId;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.GlassRepositoty;
import iuh.fit.se.repositories.NotificationRepository;
import iuh.fit.se.repositories.OrderItemRepository;
import iuh.fit.se.repositories.OrderRepository;
import iuh.fit.se.repositories.UserRepository;
import iuh.fit.se.services.VNPayService;
import iuh.fit.se.utils.VNPayUtils;

@Service
public class VNPayServiceImpl implements VNPayService {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
	private GlassRepositoty glassRepository;
	
	@Autowired
	private OrderItemRepository orderItemRepository;
	
	@Autowired
	private VNPayConfig vnpayConfig;
	
	@Override
	public String createVNPayUrl(OrderRequest orderRequest) {
		User user = userRepository.findById(orderRequest.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Order order = new Order();
		order.setUser(user);
		order.setOrderNumber(createOrderNumber());
		order.setShippingAddress(orderRequest.getShippingAddress());
		order.setPaymentMethod(orderRequest.getPaymentMethod());
		order.setOrderDate(orderRequest.getOrderDate());
		order.setStatus(orderRequest.getStatus());
		order.setTotalAmount(calculateTotal(orderRequest.getOrderItems()));

		Order savedOrder = orderRepository.save(order);
		StringBuilder productTable = new StringBuilder();
		for (OrderItemRequest item : orderRequest.getOrderItems()) {
			Glass glass = glassRepository.findById(item.getProductId())
					.orElseThrow(() -> new RuntimeException("Glass not found"));
			OrderItem orderItem = new OrderItem();

			OrderItemId orderItemId = new OrderItemId(order.getId(), glass.getId());
			orderItem.setOrderItemId(orderItemId);
			orderItem.setOrder(savedOrder);
			orderItem.setProduct(glass);
			orderItem.setQuantity(item.getQuantity());
			orderItem.setUnitPrice(item.getPrice());
			orderItem.setTotalPrice(item.getQuantity() * item.getPrice());
//			glass.setStock(glass.getStock() - item.getQuantity());
//			glassRepository.save(glass);
			orderItemRepository.save(orderItem);
		}
		Map<String, String> vnpParams = new HashMap<>();
		vnpParams.put("vnp_Version", "2.1.0");
		vnpParams.put("vnp_Command", "pay");
		vnpParams.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());
		vnpParams.put("vnp_Amount", String.valueOf((long) (order.getTotalAmount() * 100))); // Số tiền phải *
		vnpParams.put("vnp_BankCode", "NCB");																							// 100
		vnpParams.put("vnp_CurrCode", "VND");
		vnpParams.put("vnp_TxnRef", order.getOrderNumber()); // Mã giao dịch duy nhất
		vnpParams.put("vnp_OrderInfo", "Thanhtoandonhang" + order.getOrderNumber());
		vnpParams.put("vnp_OrderType", "billpayment");
		vnpParams.put("vnp_IpAddr", vnpayConfig.getVnpIpAddr());
		vnpParams.put("vnp_Locale", "vn");
		vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
		Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
	    
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));
		cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);
        

		// Tạo query string
		String query = VNPayUtils.buildQuery(vnpParams, vnpayConfig.getVnpHashSecret(), vnpayConfig.getVnpUrl());
		return query;
	}
	private Double calculateTotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }
	private String createOrderNumber() {
		LocalDateTime now = LocalDateTime.now();
		String number = now.getYear() + "" + now.getMonthValue() + "" + now.getDayOfMonth() + "" + now.getHour() + ""
				+ now.getMinute() + "" + now.getSecond();
		return number;
	}
}
