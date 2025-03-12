package iuh.fit.se.services.impl;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import iuh.fit.se.dtos.NotificationMessage;
import iuh.fit.se.dtos.OrderItemRequest;
import iuh.fit.se.dtos.OrderRequest;
import iuh.fit.se.dtos.OrderStatistic;
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
import iuh.fit.se.services.OrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GlassRepositoty glassRepository;
	
	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
    private NotificationRepository notificationRepository;
	
	@Autowired
    private JavaMailSender mailSender;
	
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Override
	public void createOrder(OrderRequest orderRequest) {
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
			glass.setStock(glass.getStock() - item.getQuantity());
			glassRepository.save(glass);
			orderItemRepository.save(orderItem);
			
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
		        glass.getName(),
		        item.getColorName(),
		        item.getQuantity(),
		        formatCurrency(item.getPrice())
		        ));
			System.out.println("Product Table: " + productTable.toString());
		}	
		
		sendNotificationEmail(user.getEmail(), order, productTable);
		
		System.out.println("Order: " + order);
		
		Notification notification = new Notification();
		notification.setOrder(order);
		notification.setMessage("New Order: #" + order.getOrderNumber());
		notificationRepository.save(notification);
		
		messagingTemplate.convertAndSend("/topic/orders", notification);
		
	}
	

	private void sendNotificationEmail(String email, Order order, StringBuilder productTable) {
        String subject = "Xác nhận đơn hàng #" + order.getOrderNumber();
//        String message = "Cảm ơn bạn đã đặt hàng! Đơn hàng #" + order.getId() 
//        + " của bạn đã được ghi nhận. Tổng tiền: $" + order.getTotalAmount();
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
	private Double calculateTotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }
	private String createOrderNumber() {
		LocalDateTime now = LocalDateTime.now();
		String number = now.getYear() + "" + now.getMonthValue() + "" + now.getDayOfMonth() + "" + now.getHour() + "" + now.getMinute() + "" + now.getSecond();
		return number;
	}


	@Override
	public Order findByOrderNumber(String orderNumber) {
		return orderRepository.findByOrderNumber(orderNumber);
	}


	@Override
	public List<Order> findAll() {
		return orderRepository.findAll();
	}


	@Override
	public Order findById(Long id) {
		return orderRepository.findById(id).orElse(null);
	}


	@Override
	public boolean deleteById(Long id) {
		if(this.findById(id)!= null) {
			orderRepository.deleteById(id);
            return true;
		}
		return false;
	}


	@Override
	public Order save(Order order) {
		return orderRepository.save(order);
	}


	@Override
	public List<Order> filterOrders(String keyword, String status, String sort) {
		 Specification<Order> spec = Specification.where(null);

	        // Thêm điều kiện tìm kiếm theo keyword nếu có
	        if (keyword != null && !keyword.isEmpty()) {
	            spec = spec.and(containsKeyword(keyword));
	        }

	        // Thêm điều kiện lọc theo trạng thái nếu có
	        if (status != null && !status.isEmpty()) {
	            spec = spec.and(hasStatus(status));
	        }

	        // Xác định thứ tự sắp xếp
	        Sort sortOption = Sort.unsorted();
	        if ("asc".equals(sort)) {
	            sortOption = Sort.by("totalAmount").ascending(); // Sắp xếp giá tăng dần
	        } else if ("desc".equals(sort)) {
	            sortOption = Sort.by("totalAmount").descending(); // Sắp xếp giá giảm dần
	        }

	        // Trả về danh sách kết quả sau khi lọc
	        return orderRepository.findAll(spec, sortOption);
	}
	
	  // Tìm kiếm theo từ khóa trong các trường cụ thể
    public static Specification<Order> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("id").as(String.class)), likePattern), // Tìm theo ID
                criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), likePattern),         // Tìm theo Order Number
                criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("fullname")), likePattern),  // Tìm theo tên User
                criteriaBuilder.like(criteriaBuilder.lower(root.get("shippingAddress")), likePattern),    // Tìm theo địa chỉ giao hàng
                criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), likePattern)       // Tìm theo phương thức thanh toán
            );
        };
    }
    // Lọc theo trạng thái
    public static Specification<Order> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), status);
    }


	@Override
	public List<OrderStatistic> getPurchasedOrder() {
		List<Object[]> list = orderRepository.getPurchasedOrder();
		return list.stream().map(item -> new OrderStatistic((Integer) item[0], (Integer) item[1])).collect(Collectors.toList());
	}


	@Override
	public List<OrderStatistic> getSalesOrder() {
		List<Object[]> list = orderRepository.getSalesOrder();
		return list.stream().map(item -> new OrderStatistic((Integer) item[0], (Integer) item[1])).collect(Collectors.toList());
	}
	
	public List<Map<String, Object>> getMonthlyRevenue(int year) {
        List<Object[]> results = orderRepository.findMonthlyRevenue(year);
        Map<Integer, Double> revenueMap = new HashMap<>();

        // Gán dữ liệu query vào map (month -> revenue)
        for (Object[] result : results) {
            int month = (int) result[0];
            double totalRevenue = (double) result[2];
            revenueMap.put(month, totalRevenue);
        }

        // Tạo danh sách đầy đủ 12 tháng
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("month", i);
            data.put("year", year);
            data.put("totalRevenue", revenueMap.getOrDefault(i, 0.0)); // Doanh thu = 0 nếu không có
            monthlyRevenue.add(data);
        }

        return monthlyRevenue;
    }

    // Lấy doanh thu theo ngày trong một tháng
    public List<Map<String, Object>> getDailyRevenueInMonth(int year, int month) {
        List<Object[]> results = orderRepository.findDailyRevenueInMonth(year, month);
        Map<Integer, Double> revenueMap = new HashMap<>();

        // Gán dữ liệu query vào map (day -> revenue)
        for (Object[] result : results) {
            int day = (int) result[0];
            double totalRevenue = (double) result[1];
            revenueMap.put(day, totalRevenue);
        }

        // Xác định số ngày trong tháng
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("day", i);
            data.put("totalRevenue", revenueMap.getOrDefault(i, 0.0)); // Doanh thu = 0 nếu không có
            dailyRevenue.add(data);
        }

        return dailyRevenue;
    }
    public Map<String, Double> getStatusPercentageByMonth(int year, int month) {
        List<Object[]> results = orderRepository.countOrdersByStatusInMonth(year, month);
        return calculatePercentage(results);
    }

    public Map<String, Double> getStatusPercentageByYear(int year) {
        List<Object[]> results = orderRepository.countOrdersByStatusInYear(year);
        return calculatePercentage(results);
    }
    private Map<String, Double> calculatePercentage(List<Object[]> results) {
        Map<String, Long> statusCountMap = new HashMap<>();
        long total = 0;

        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statusCountMap.put(status, count);
            total += count;
        }

        Map<String, Double> percentageMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : statusCountMap.entrySet()) {
            String status = entry.getKey();
            Long count = entry.getValue();
            percentageMap.put(status, (double) count / total * 100);
        }

        return percentageMap;
    }

	@Override
	public List<Order> getOrdersByYearAndMonth(int year, Integer month) {
		List<Order> orders;
        if (month != null) {
            orders = orderRepository.findByYearAndMonth(year, month);
        } else {
            orders = orderRepository.findByYear(year);
        }
		return orders;
	}


		@Override
		public void exportOrderData(List<Order> orders, HttpServletResponse response) throws IOException {
			Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Orders");
	
	        // Tạo tiêu đề cho file Excel
	        Row titleRow = sheet.createRow(0);
	        Cell titleCell = titleRow.createCell(0);
	        titleCell.setCellValue("Order Report");

	        // Định dạng tiêu đề
	        CellStyle titleStyle = workbook.createCellStyle();
	        Font titleFont = workbook.createFont();
	        titleFont.setBold(true);
	        titleFont.setFontHeightInPoints((short) 16); // Kích thước chữ lớn hơn
	        titleStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
	        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc
	        titleStyle.setFont(titleFont);
	        titleCell.setCellStyle(titleStyle);

	        // Hợp nhất các cột cho tiêu đề
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
	        
	        int currentRowIndex = 2;
	        
	        // Tạo Header
	        Row headerRow = sheet.createRow(currentRowIndex++);
	        String[] headers = {"Order Number", "Customer", "Order Date", "Shipping Address", "Payment Method", "Status", "Total Amount"};
	        CellStyle headerStyle = workbook.createCellStyle();
	        Font font = workbook.createFont();
	        font.setBold(true);
	        headerStyle.setFont(font);
	
	        for (int i = 0; i < headers.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(headers[i]);
	            cell.setCellStyle(headerStyle);
	        }
	
	        // Điền dữ liệu
	        double totalRevenue = 0;
	        for (Order order : orders) {
	            Row row = sheet.createRow(currentRowIndex++);
	            
	            row.createCell(0).setCellValue(order.getOrderNumber());
	            row.createCell(1).setCellValue(order.getUser().getFullname()); // User giả định có trường "name"
	            row.createCell(2).setCellValue(order.getOrderDate().toString());
	            row.createCell(3).setCellValue(order.getShippingAddress());
	            row.createCell(4).setCellValue(order.getPaymentMethod());
	            row.createCell(5).setCellValue(order.getStatus());
	            row.createCell(6).setCellValue(order.getTotalAmount());
	            
	         // Tính tổng doanh thu
	            totalRevenue += order.getTotalAmount();
	        }
	        
	        currentRowIndex += 2;
	
	     // Thêm dòng tổng doanh thu
	        Row totalRow = sheet.createRow(currentRowIndex);
	        Cell totalLabelCell = totalRow.createCell(5); // Cột "Status"
	        Cell totalValueCell = totalRow.createCell(6); // Cột "Total Amount"

	        totalLabelCell.setCellValue("Total Revenue");
	        CellStyle totalStyle = workbook.createCellStyle();
	        Font totalFont = workbook.createFont();
	        totalFont.setBold(true);
	        totalStyle.setFont(totalFont);
	        totalLabelCell.setCellStyle(totalStyle);

	        totalValueCell.setCellValue(totalRevenue);
	        totalValueCell.setCellStyle(totalStyle);
	        // Tự động điều chỉnh độ rộng cột
	        for (int i = 0; i < headers.length; i++) {
	            sheet.autoSizeColumn(i);
	        }
	
	        // Thiết lập header response
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
	
	        // Ghi dữ liệu ra output stream
	        ServletOutputStream outputStream = response.getOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        outputStream.close();
			
		}


		@Override
		public List<Order> findOrdersByUserName(String userName) {
			return orderRepository.findOrdersByUserName(userName);
		}
}
