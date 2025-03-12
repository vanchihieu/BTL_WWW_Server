package iuh.fit.se.dtos;

import java.util.List;

public class OrderDataStatistic {
	private List<Integer> purchasedOrder;
	private List<Integer> salesOrder;
	private List<String> month;
	public OrderDataStatistic(List<Integer> purchasedOrder, List<Integer> salesOrder, List<String> month) {
		super();
		this.purchasedOrder = purchasedOrder;
		this.salesOrder = salesOrder;
		this.month = month;
	}
	public OrderDataStatistic() {
		super();
	}
	public List<Integer> getPurchasedOrder() {
		return purchasedOrder;
	}
	public void setPurchasedOrder(List<Integer> purchasedOrder) {
		this.purchasedOrder = purchasedOrder;
	}
	public List<Integer> getSalesOrder() {
		return salesOrder;
	}
	public void setSalesOrder(List<Integer> salesOrder) {
		this.salesOrder = salesOrder;
	}
	public List<String> getMonth() {
		return month;
	}
	public void setMonth(List<String> month) {
		this.month = month;
	}
	
}
