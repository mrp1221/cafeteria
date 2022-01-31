package code;

import java.util.ArrayList;
import java.util.List;

public class Order {
	private List<Food> order;
	private int orderNum;
	
	public Order(List<Food> order, int orderNum) {
		this.order = order;
		this.orderNum = orderNum;
	}
	
	public int orderNum() {
		return orderNum;
	}

	public List<Food> order() {
		return new ArrayList<Food>(order);
	}
}
