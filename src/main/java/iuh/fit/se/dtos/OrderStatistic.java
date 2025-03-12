package iuh.fit.se.dtos;

public class OrderStatistic {
	private int month;
	private int count;
	public OrderStatistic(int month, int count) {
		super();
		this.month = month;
		this.count = count;
	}
	public OrderStatistic() {
		super();
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "OrderStatistic [month=" + month + ", count=" + count + "]";
	}
	
	
}
