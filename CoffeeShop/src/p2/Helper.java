package p2;

import java.util.List;

public class Helper {

	private Customer customer;
	private List<Food> order;
	private int orderNumber;
	
	public Helper(Customer cust, List<Food> ord, int orderNum){
		this.customer = cust;
		this.order = ord;
		this.orderNumber = orderNum;
	}

	public Customer getCustomer() {
		return customer;
	}

	public List<Food> getOrder() {
		return order;
	}

	public int getOrderNumber() {
		return orderNumber;
	}
	
	
}
