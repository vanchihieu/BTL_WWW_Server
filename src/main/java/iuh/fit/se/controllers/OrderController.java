package iuh.fit.se.controllers;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.dtos.OrderDataStatistic;
import iuh.fit.se.dtos.OrderRequest;
import iuh.fit.se.dtos.OrderStatistic;
import iuh.fit.se.entities.Notification;
import iuh.fit.se.entities.Order;
import iuh.fit.se.repositories.NotificationRepository;
import iuh.fit.se.repositories.OrderItemRepository;
import iuh.fit.se.repositories.OrderRepository;
import iuh.fit.se.services.OrderItemService;
import iuh.fit.se.services.OrderService;
import iuh.fit.se.services.VNPayService;
import iuh.fit.se.utils.VNPayUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
    private NotificationRepository notificationRepository;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
    private JavaMailSender mailSender;
	
	@Autowired
	private OrderItemService  orderItemService;
	
	@Autowired
	private VNPayService vnpayService;
	private static final String VNP_HASH_SECRET = "1BX5VZOVDP8G19NJCYVC4OZW2V5XXLZG";
	private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
	@PostMapping("/checkout")
	public ResponseEntity<Map<String, Object>> checkout(@RequestBody OrderRequest orderRequest) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		try {
			if(orderRequest.getPaymentMethod().equalsIgnoreCase("VNPAY")) {
				String vnpUrl = vnpayService.createVNPayUrl(orderRequest);
				response.put("status", HttpStatus.OK.value());
				response.put("message", "Redirect to VNPAY");
				response.put("paymenUrl", vnpUrl);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}else {
				orderService.createOrder(orderRequest);
				response.put("status", HttpStatus.OK.value());
				response.put("message", "Order created successfully");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.put("message", "Internal server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	@GetMapping("/vnpay-return")
	public ResponseEntity<Void> handleVNPayCallback(@RequestParam Map<String, String> allParams, HttpServletResponse responseServlet) throws IOException {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
	    String txnRef = allParams.get("vnp_TxnRef"); // Lấy mã giao dịch
	    String responseCode = allParams.get("vnp_ResponseCode");

	    // Kiểm tra chữ ký
	    String secureHash = allParams.remove("vnp_SecureHash");
	    String calculatedHash = VNPayUtils.buildQuery(allParams, VNP_HASH_SECRET, VNP_URL);
	    Map<String, String> params = extractParams(calculatedHash);
	    String hash = params.get("vnp_SecureHash");
	    if (!secureHash.equals(hash)) {
//	    	response.put("status", HttpStatus.BAD_REQUEST.value());
//	    	response.put("message", "Invalid signature!");
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    	
	    	responseServlet.sendRedirect("http://localhost:8081/payment-result?status=failed&txnRef=" + txnRef);
	    }

	    // Tìm đơn hàng dựa vào txnRef
	    Order order = orderService.findByOrderNumber(txnRef);
	    
	    if ("00".equals(responseCode)) {
	        // Thanh toán thành công, cập nhật trạng thái đơn hàng
	        order.setStatus("PAID");
	        orderRepository.save(order);
//	        response.put("status", HttpStatus.OK.value());
//	        response.put("message", "Payment successful!");
//	        return ResponseEntity.ok(response);
	        StringBuilder productTable = new StringBuilder();
			order.getOrderItems().forEach(item -> {
				item.getProduct().setStock(item.getProduct().getStock() - item.getQuantity());
				productTable.append(String.format("""
			            <tr>
			                <td style="padding: 10px; border-top: 1px solid #ddd;">
			                    <div style="display: flex; align-items: center;">
			                        <div>
			                            <div style="font-weight: bold;">%s</div>
			                        </div>
			                    </div>
			                </td>
			                <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%s</td>
			                <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%d</td>
			                <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%s</td>
			            </tr>
			        """, 
			        item.getProduct().getName(),
			        item.getProduct().getColorName(),
			        item.getQuantity(),
			        formatCurrency(item.getProduct().getPrice())
			        ));
			});
			
			sendNotificationEmail(order.getUser().getEmail(), order, productTable);
			
			
	        Notification notification = new Notification();
			notification.setOrder(order);
			notification.setMessage("New Order: #" + order.getOrderNumber());
			notificationRepository.save(notification);
			
			messagingTemplate.convertAndSend("/topic/orders", notification);
	        responseServlet.sendRedirect("http://localhost:8081/payment-result?status=success&txnRef=" + txnRef);
	    } else {
	        // Thanh toán thất bại
	        order.setStatus("FAILED");
	        orderRepository.save(order);
//	        response.put("status", HttpStatus.BAD_REQUEST.value());
//	        response.put("message", "Payment failed!");
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        Notification notification = new Notification();
			notification.setOrder(order);
			notification.setMessage("Đơn hàng mới: #" + order.getOrderNumber());
			notificationRepository.save(notification);
			
			messagingTemplate.convertAndSend("/topic/orders", notification);
	        responseServlet.sendRedirect("http://localhost:8081/payment-result?status=failed&txnRef=" + txnRef);
	    }
	    return ResponseEntity.ok().build();
	}
	 public static Map<String, String> extractParams(String url) {
	        Map<String, String> params = new HashMap<>();
	        try {
	            // Tách phần query string (phần sau dấu ?)
	            String queryString = url.split("\\?")[1];

	            // Phân tách các cặp key=value
	            String[] pairs = queryString.split("&");
	            for (String pair : pairs) {
	                String[] keyValue = pair.split("=");
	                String key = URLDecoder.decode(keyValue[0], "UTF-8");
	                String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
	                params.put(key, value);
	            }
	        } catch (Exception e) {
	            throw new RuntimeException("Lỗi khi trích xuất tham số từ URL", e);
	        }
	        return params;
	    }
	 private void sendNotificationEmail(String email, Order order, StringBuilder productTable) {
	        String subject = "Xác nhận đơn hàng #" + order.getOrderNumber();
//	        String message = "Cảm ơn bạn đã đặt hàng! Đơn hàng #" + order.getId() 
//	        + " của bạn đã được ghi nhận. Tổng tiền: $" + order.getTotalAmount();
	        System.out.println("Email: " + email);
	        System.out.println( order.getOrderDate().toString());
	        String htmlTemplate = """
	                <!DOCTYPE html>
	                <html>
	                <head>
	                    <style>
	                        body {
	                            font-family: Arial, sans-serif;
	                            background-color: #f4f4f4;
	                            margin: 0;
	                            padding: 0;
	                        }
	                        .email-container {
	                            width: 100%%;
	                            max-width: 600px;
	                            margin: 0 auto;
	                            background-color: #ffffff;
	                            padding: 20px;
	                            border: 1px solid #ddd;
	                            border-radius: 8px;
	                        }
	                        .header {
	                            text-align: center;
	                            padding-bottom: 20px;
	                            border-bottom: 1px solid #eee;
	                        }
	                        .header img {
	                            width: 150px;
	                        }
	                        .content {
	                            padding: 20px 0;
	                        }
	                        .content h2 {
	                            color: #4CAF50;
	                        }
	                        .order-info {
	                            background-color: #f9f9f9;
	                            padding: 10px;
	                            margin-bottom: 20px;
	                            border-radius: 8px;
	                        }
	                        .products-table {
	                            width: 100%%;
	                            border-collapse: collapse;
	                            margin-bottom: 20px;
	                        }
	                        .products-table th {
	                            background-color: #f2f2f2;
	                            padding: 10px;
	                            text-align: left;
	                        }
	                        .products-table td {
	                            padding: 10px;
	                            border-bottom: 1px solid #eee;
	                        }
	                        .products-table td:last-child {
	                            text-align: right;
	                        }
	                        .amount-section {
	                            padding: 10px 0;
	                            border-top: 1px solid #eee;
	                        }
	                        .amount-section table {
	                            width: 100%%;
	                        }
	                        .amount-section td {
	                            padding: 5px 0;
	                        }
	                        .total-amount {
	                            color: #E91E63;
	                            font-size: 18px;
	                            font-weight: bold;
	                        }
	                        .footer {
	                            text-align: center;
	                            padding-top: 20px;
	                            font-size: 12px;
	                            color: #666;
	                        }
	                        .footer p {
	                            margin: 5px 0;
	                        }
	                        .footer hr {
	                            border: none;
	                            border-top: 1px solid #eee;
	                            margin: 10px 0;
	                        }
	                    </style>
	                </head>
	                <body>
	                    <div class="email-container">
	                        <div class="header">
	                            <img src="cid:logo" alt="Logo" width='1200' height='50'>
	                            <h2 style="margin: 0;">Xác nhận đơn hàng #%s</h2>
	                        </div>
	                        
	                        <div class="content">
	                            <h2>Xin chào %s,</h2>
	                            <p>Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi!</p>
	                            
	                            <div class="order-info">
	                                <h3 style="margin-top: 0;">Thông tin đơn hàng:</h3>
	                                <p><strong>Mã đơn hàng:</strong> #%d</p>
	                                <p><strong>Ngày đặt hàng:</strong> %s</p>
	                                <p><strong>Phương thức thanh toán:</strong> %s</p>
	                            </div>

	                            <h3>Chi tiết đơn hàng</h3>
	                            <table class="products-table">
	                                <thead>
	                                    <tr>
	                                        <th>Sản phẩm</th>
	                                        <th style="text-align: right;">Màu</th>
	                                        <th style="text-align: right;">Số lượng</th>
	                                        <th style="text-align: right;">Giá</th>
	                                    </tr>
	                                </thead>
	                                <tbody>
	                                    %s
	                                </tbody>
	                            </table>

	                            <div class="amount-section">
	                                <table style="width: 100%%;">
	                                    <tr>
	                                        <td>Tạm tính:</td>
	                                        <td style="text-align: right;">%s</td>
	                                    </tr>
	                                    <tr>
	                                        <td>Phí vận chuyển:</td>
	                                        <td style="text-align: right;">0đ</td>
	                                    </tr>
	                                    <tr>
	                                        <td style="font-weight: bold; font-size: 18px;">Tổng cộng:</td>
	                                        <td style="text-align: right;"><span class="total-amount">%s</span></td>
	                                    </tr>
	                                </table>
	                            </div>
	                        </div>

	                        <div class="footer">
	                            <p><strong>Cần hỗ trợ?</strong></p>
	                            <p>Email: support@yourstore.com | Hotline: 096.689.4644</p>
	                            <p>Địa chỉ: 112 Cao Thắng, Quận 3 – HCM</p>
	                            <hr>
	                            <p>© 2024 Eyeglasses Store. All rights reserved.</p>
	                            <p style="color: #999;">Email này được gửi tự động, vui lòng không trả lời.</p>
	                        </div>
	                    </div>
	                </body>
	                </html>
	            """.formatted(
	                order.getOrderNumber(),
	                order.getUser().getFullname(),
	                order.getId(),
	                order.getOrderDate().toString(),
	                order.getPaymentMethod(),
	                productTable.toString(),
	                formatCurrency(order.getTotalAmount()),
	                formatCurrency(order.getTotalAmount())
	            );


	        
	        MimeMessage messageMail = mailSender.createMimeMessage();
	        try {
	        	MimeMessageHelper helper = new MimeMessageHelper(messageMail, true);
	        	helper.setFrom("sendingemaileventhub@gmail.com");
	        	helper.setTo(email);
	        	helper.setSubject(subject);
	        	helper.setText(htmlTemplate, true);
	        	ClassPathResource image = new ClassPathResource("static/images/logo.png");
	        	helper.addInline("logo", image);
	        	mailSender.send(messageMail);
			} catch (MessagingException e) {
				e.printStackTrace();
			} 
		}
	 	public String formatCurrency(double amount) {
		    NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
		    return currencyFormat.format(amount) + "đ";
		}
	    @GetMapping("/all")
		public ResponseEntity<Map<String, Object>> getAllOrders() {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			List<Order> orders = orderService.findAll();
			orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
			response.put("status", HttpStatus.OK.value());
			response.put("data", orders);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	    
	    @GetMapping("/order")
	    public ResponseEntity<Map<String, Object>> getOrderItemByOrderId(@RequestParam Long id) {
	    	Map<String, Object> response = new LinkedHashMap<String, Object>();
	    	response.put("status", HttpStatus.OK.value());
	    	response.put("data", orderItemService.findByOrderItemIdOrderId(id));
	    	return ResponseEntity.status(HttpStatus.OK).body(response);
	    }
	    
	    @GetMapping("/{id}")
		public ResponseEntity<Map<String, Object>> findOrderById(@PathVariable Long id) {
        	Map<String, Object> response = new LinkedHashMap<String, Object>();
        	response.put("status", HttpStatus.OK.value());
        	response.put("data", orderService.findById(id));
        	return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	    
	    @DeleteMapping("/delete/{id}")
		public ResponseEntity<Map<String, Object>> deleteOrderById(@PathVariable Long id) {
        	Map<String, Object> response = new LinkedHashMap<String, Object>();
        	response.put("status", HttpStatus.OK.value());
        	response.put("data", orderService.deleteById(id));
        	return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	    
	    @GetMapping("/update-status/{id}")
		public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id,
				@RequestParam String status) {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			System.out.println(id + " | " + status);
			Order order = orderService.findById(id);
			order.setStatus(status);
			orderService.save(order);
			response.put("status", HttpStatus.OK.value());
			response.put("message", "Order status updated successfully");
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	    
	    @GetMapping("/filter")
	    public ResponseEntity<Map<String, Object>> filterOrders(@RequestParam(value = "keyword", required = false) String keyword,
	            @RequestParam(value = "status", required = false) String status,
	            @RequestParam(value = "sort", required = false) String sort) {
	        Map<String, Object> response = new LinkedHashMap<String, Object>();
	        response.put("status", HttpStatus.OK.value());
	        response.put("data", orderService.filterOrders(keyword, status, sort));
	        return ResponseEntity.status(HttpStatus.OK).body(response);
	    }
	    
	    @GetMapping("/orders-statistic")
		public ResponseEntity<Map<String, Object>> getOrderStatistic() {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			OrderDataStatistic data = new OrderDataStatistic();
			List<OrderStatistic> purchasedOrder = orderService.getPurchasedOrder();
			List<OrderStatistic> salesOrder = orderService.getSalesOrder();
			Integer[] monthlyPurchase = new Integer[12];
			Arrays.fill(monthlyPurchase, 0);
			Integer[] monthlySales = new Integer[12];
			Arrays.fill(monthlySales, 0);
			for (OrderStatistic order : purchasedOrder) {
				monthlyPurchase[order.getMonth() - 1] = order.getCount();
			}
			for (OrderStatistic order : salesOrder) {
				monthlySales[order.getMonth() - 1] = order.getCount();
			}
			data.setPurchasedOrder(Arrays.asList(monthlyPurchase));
			data.setSalesOrder(Arrays.asList(monthlySales));
			data.setMonth(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
			
			response.put("status", HttpStatus.OK.value());
			response.put("data", data);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	    
	    @GetMapping("/revenue/monthly")
	    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(@RequestParam int year) {
	        return ResponseEntity.ok(orderService.getMonthlyRevenue(year));
	    }

	    // API thống kê doanh thu theo ngày trong tháng
	    @GetMapping("/revenue/daily")
	    public ResponseEntity<List<Map<String, Object>>> getDailyRevenueInMonth(@RequestParam int year, @RequestParam int month) {
	        return ResponseEntity.ok(orderService.getDailyRevenueInMonth(year, month));
	    }
	    @GetMapping("/status-percentage/monthly")
	    public ResponseEntity<Map<String, Double>> getStatusPercentageByMonth(
	            @RequestParam int year, 
	            @RequestParam int month) {
	        return ResponseEntity.ok(orderService.getStatusPercentageByMonth(year, month));
	    }

	    @GetMapping("/status-percentage/yearly")
	    public ResponseEntity<Map<String, Double>> getStatusPercentageByYear(@RequestParam int year) {
	        return ResponseEntity.ok(orderService.getStatusPercentageByYear(year));
	    }
	    @GetMapping("/list")
		public ResponseEntity<List<Order>> findByYearAndMonth(@RequestParam int year, @RequestParam(required = false) Integer month) {
			return ResponseEntity.ok(orderService.getOrdersByYearAndMonth(year, month));
		}
	    @GetMapping("/orders-export")
	    public void exportOrders(@RequestParam int year, @RequestParam(required = false) Integer month, HttpServletResponse  response) throws IOException {
	    	System.out.println("Exporting orders");
	    	List<Order> orders = orderService.getOrdersByYearAndMonth(year, month);
	    	orderService.exportOrderData(orders, response);
	    }
	    
	    @GetMapping("/orders-history")
		public ResponseEntity<Map<String, Object>> getOrderByUserName(@RequestParam String userName) {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			response.put("status", HttpStatus.OK.value());
			response.put("data", orderService.findOrdersByUserName(userName));
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
}
