package iuh.fit.se.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iuh.fit.se.dtos.OrderStatistic;
import iuh.fit.se.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>{
	public Order findByOrderNumber(String orderNumber);
	
	@Query(value = "select MONTH(order_date) as month,COUNT(*) as count from orders WHERE status IN ('PENDING', 'PROCESSING', 'PAID') GROUP BY MONTH(order_date)", nativeQuery = true)
	public List<Object[]> getPurchasedOrder();
	@Query(value = "select MONTH(order_date) as month,COUNT(*) as count from orders WHERE status IN ('COMPLETED', 'SHIPPED') GROUP BY MONTH(order_date)", nativeQuery = true)
	public List<Object[]> getSalesOrder();
	
	  // Thống kê doanh thu theo từng tháng
    @Query("SELECT MONTH(o.orderDate) AS month, YEAR(o.orderDate) AS year, SUM(o.totalAmount) AS totalRevenue " +
           "FROM Order o " +
           "WHERE YEAR(o.orderDate) = :year " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<Object[]> findMonthlyRevenue(@Param("year") int year);
    
 // Thống kê doanh thu trong một tháng cụ thể
    @Query("SELECT DAY(o.orderDate) AS day, SUM(o.totalAmount) AS totalRevenue " +
           "FROM Order o " +
           "WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month " +
           "GROUP BY DAY(o.orderDate) " +
           "ORDER BY DAY(o.orderDate)")
    List<Object[]> findDailyRevenueInMonth(@Param("year") int year, @Param("month") int month);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month GROUP BY o.status")
    List<Object[]> countOrdersByStatusInMonth(@Param("year") int year, @Param("month") int month);

    // Đếm tổng số đơn hàng trong năm, nhóm theo trạng thái
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE YEAR(o.orderDate) = :year GROUP BY o.status")
    List<Object[]> countOrdersByStatusInYear(@Param("year") int year);
    
    @Query("SELECT o FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month ORDER BY o.orderDate DESC")
    List<Order> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT o FROM Order o WHERE YEAR(o.orderDate) = :year ORDER BY o.orderDate DESC")
    List<Order> findByYear(@Param("year") int year);
    
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.username = :userName ORDER BY o.orderDate DESC")
    List<Order> findOrdersByUserName(@Param("userName") String userName);
}
