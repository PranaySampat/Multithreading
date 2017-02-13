package p2;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;
	public final int machineCapacity;
	public final int cookingTime;
	//private static int currentCapacity = 0;
	private int numberOfItems;
	private int orderNumber = 0;

	//YOUR CODE GOES HERE...


	/**
	 * The constructor takes at least the name of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn, int cookingTime) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		this.machineCapacity = capacityIn;
		this.cookingTime = cookingTime;
	
		//YOUR CODE GOES HERE...

	}
	
	public String toString(){
		return machineName;
	}

	

	



	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it t[ake extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public Object makeFood(int quantity, int orderNumber) throws InterruptedException {
		//YOUR CODE GOES HERE...
		Thread[] itemMake = new Thread[quantity];
		for(int i =0; i<itemMake.length;i++){
			itemMake[i] = new Thread(new CookAnItem());
		}
		for(int i =0; i<itemMake.length;i++){
			itemMake[i].start();
		}
		for(int i =0; i<itemMake.length;i++){
			itemMake[i].join();
		}
		
			synchronized (Simulation.hashMapLock) {
				Simulation.totalOrder.put(orderNumber, Simulation.totalOrder.get(orderNumber) - quantity);
				//SimulationEvent.machineDoneFood(this, this.machineFoodType);
		}
		
		
			return null;
	}

	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		public  int currentCapacity = 0;
		public void run() {
			synchronized (this) {
			try {
				//YOUR CODE GOES HERE...
				
					while(currentCapacity >= machineCapacity){
						CookAnItem.this.wait();
					}
					//System.out.println(currentCap);
					currentCapacity ++; 
					Simulation.logEvent(SimulationEvent.machineCookingFood(Machine.this, machineFoodType));
					//System.out.println(currentCapacity);
					Thread.sleep(cookingTime);
					Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
					
					currentCapacity --;
					notifyAll();
				}
				
			 catch(InterruptedException e) { }
			
		}
		
		}
		
	
 

	public String toString() {
		return machineName;
	}
}
}