package iuh.fit.se.dtos;

public class GlassStatistic {
	private Long id;
	private String name;
	private int count;
	public GlassStatistic(Long id, String name, int count) {
		super();
		this.id = id;
		this.name = name;
		this.count = count;
	}
	public GlassStatistic() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "GlassStatistic [id=" + id + ", name=" + name + ", count=" + count + "]";
	}
	
	
}
