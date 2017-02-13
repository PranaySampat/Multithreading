package p2;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import p2.FoodType;

/**
 * Simulation is the main class used to run the simulation.  You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static final Object queueLock = new Object();
	public static final Object hashMapLock = new Object();
	public static final Object newLock = new Object();
	public static List<SimulationEvent> events;  
	private static  Queue<Helper> helperQueue;
	public static HashMap<Customer,Boolean> customerOrderStatus;
	//public static CookOrderStatus<Cook,Integer> cookOrderStatus;
	public static HashMap<Integer,Integer> totalOrder;
	
	private static Machine machineGrill;
	private static Machine coffeeMaker;
	private static Machine friesMachine;

	static{
		helperQueue = new LinkedList<>();
		customerOrderStatus = new HashMap<>();
		totalOrder = new HashMap<>();
	}
	

	public static  Queue<Helper> getHelperQueue() {
		synchronized (queueLock) {
			return helperQueue;
		}
		
	}


	


	/**
	 * Used by other classes in the simulation to log events
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * 	Function responsible for performing the simulation. Returns a List of 
	 *  SimulationEvent objects, constructed any way you see fit. This List will
	 *  be validated by a call to Validate.validateSimulation. This method is
	 *  called from Simulation.main(). We should be able to test your code by 
	 *  only calling runSimulation.
	 *  
	 *  Parameters:
	 *	@param numCustomers the number of customers wanting to enter the coffee shop
	 *	@param numCooks the number of cooks in the simulation
	 *	@param numTables the number of tables in the coffe shop (i.e. coffee shop capacity)
	 *	@param machineCapacity the capacity of all machines in the coffee shop
	 *  @param randomOrders a flag say whether or not to give each customer a random order
	 *
	 */
	public static List<SimulationEvent> runSimulation(
			int numCustomers, int numCooks,
			int numTables, 
			int machineCapacity,
			boolean randomOrders
			) {

		//This method's signature MUST NOT CHANGE.  
		

		//We are providing this events list object for you.  
		//  It is the ONLY PLACE where a concurrent collection object is 
		//  allowed to be used.
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());




		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers,
				numCooks,
				numTables,
				machineCapacity));

		// Setup the machines
		
		 machineGrill = new Machine("machineGrill", FoodType.burger, machineCapacity, 500);
		 friesMachine = new Machine("fryer", FoodType.fries, machineCapacity,350);
		 coffeeMaker = new Machine("CoffeeMaker2000", FoodType.coffee, machineCapacity,100);	
		
		//Setting up the methods
		
		// Creating cooks
		Thread[] cooks = new Thread[numCooks];
		for(int i =0; i<cooks.length;i++){
			cooks[i] = new Thread(new Cook("Cook" + i+1));
		}
		// Set things up you might need


		



		// Let cooks in


		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			order = new LinkedList<Food>();
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			for(int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(
						new Customer("Customer " + (i+1), order, numTables)
						);
			}
		}
		else {
			for(int i = 0; i < customers.length; i++) {
				Random rnd = new Random(27);
				int burgerCount = rnd.nextInt(3);
				int friesCount = rnd.nextInt(3);
				int coffeeCount = rnd.nextInt(3);
				order = new LinkedList<Food>();
				for (int b = 0; b < burgerCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int c = 0; c < coffeeCount; c++) {
					order.add(FoodType.coffee);
				}
				customers[i] = new Thread(
						new Customer("Customer " + (i+1), order, numTables)
						);
			}
		}


		// Now "let the customers know the shop is open" by
		//    starting them running in their own thread.
		for(int i = 0; i < customers.length; i++) {
			customers[i].start();
			
			//NOTE: Starting the customer does NOT mean they get to go
			//      right into the shop.  There has to be a table for
			//      them.  The Customer class' run method has many jobs
			//      to do - one of these is waiting for an available
			//      table...
		}
		// starting the cooks
		//Starting the cooks because once customer start they will order
		for(int i=0; i<cooks.length; i++){
			cooks[i].start();
		}
		
		try {
			// Wait for customers to finish
			//   -- you need to add some code here...
			
			for(int i =0 ;i<customers.length;i++){
				customers[i].join();
			}
			
			
			

			// Then send cooks home...
			// The easiest way to do this might be the following, where
			// we interrupt their threads.  There are other approaches
			// though, so you can change this if you want to.
//			for(int i = 0; i < cooks.length; i++)
//				cooks[i].interrupt();
			for(int i = 0; i < cooks.length; i++)
				cooks[i].join();

		}
		catch(InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines

		logEvent(SimulationEvent.machineEnding(machineGrill));
		logEvent(SimulationEvent.machineEnding(friesMachine));
		logEvent(SimulationEvent.machineEnding(coffeeMaker));
		



		// Done with simulation		
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation.  There
	 * should be exactly four arguments: the first is the number of customers,
	 * the second is the number of cooks, the third is the number of tables
	 * in the coffee shop, and the fourth is the number of items each cooking
	 * machine can make at the same time.  
	 * @throws InterruptedException 
	 */
	public static void cookBurger(int numberOfFood, int orderNum) throws InterruptedException{
		machineGrill.makeFood(numberOfFood,orderNum);
	}
	public static void cookFries(int quantity,int orderNum) throws InterruptedException{
		friesMachine.makeFood(quantity,orderNum);
	}
	public static void makeCoffee(int quantity,int orderNum) throws InterruptedException{
		coffeeMaker.makeFood(quantity, orderNum);
	}
	public static void main(String args[]) throws InterruptedException {
		// Parameters to the simulation
		/*
		if (args.length != 4) {
			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <capacity> <randomorders");
			System.exit(1);
		}
		int numCustomers = new Integer(args[0]).intValue();
		int numCooks = new Integer(args[1]).intValue();
		int numTables = new Integer(args[2]).intValue();
		int machineCapacity = new Integer(args[3]).intValue();
		boolean randomOrders = new Boolean(args[4]);
		 */
		int numCustomers = 6;
		int numCooks =1;
		int numTables = 5;
		int machineCapacity = 4;
		boolean randomOrders = false;
	

		// Run the simulation and then 
		//   feed the result into the method to validate simulation.
		System.out.println("Did it work? " + 
				Validate.validateSimulation(
						runSimulation(
								numCustomers, numCooks, 
								numTables, machineCapacity,
								randomOrders
								)
						)
				);
	}

}



