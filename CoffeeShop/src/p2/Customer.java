package p2;

import java.util.HashMap;
import java.util.List;

import javax.crypto.Mac;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the 
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;    
	private final int coffeeshopCapacity;
	private static int runningCounter = 0;
	private static int currentCapacity = 0;
	private static final Object capacityLock = new Object();
	
	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int numTables) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
		this.coffeeshopCapacity = numTables;
		//this.currentCapacity++;
		
		
	}
//	public Customer getCustomer(){
//		return new Customer(this.name, this.order,this.coffeeshopCapacity);
//	}
//	
	public String toString() {
		return name;
	}
	

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the coffee shop (only successful when the coffee shop has a
	 * free table), place its order, and then leave the coffee shop
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...
		
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		synchronized (this) {
			while(currentCapacity >= coffeeshopCapacity){
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			currentCapacity ++;
			Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
			synchronized (Simulation.queueLock) {
				Simulation.getHelperQueue().add(new Helper(this, this.order, orderNum));
				Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
			}
			synchronized (Simulation.hashMapLock) {
				Simulation.customerOrderStatus.put(this, false);
			
			}
				while(Simulation.customerOrderStatus.containsKey(this) && Simulation.customerOrderStatus.get(this) == false){
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
					
					Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
					notifyAll();
					currentCapacity --;
					
			
				
		}
		
		}
		
}
		
	
		

