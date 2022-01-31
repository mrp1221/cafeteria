package code;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Customer and process them.
 */
public class Cook implements Runnable {
	private final String name;
	//private final Semaphore cooksAvailable;


	/**
	 * You can feel free modify this constructor. It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful.
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name, Semaphore sem) {
		this.name = name;
		//cooksAvailable = sem;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows. The cook tries to retrieve
	 * orders placed by Customers. For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine type, by calling makeFood(). Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified. The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			
			while (true) {
				// SEE IF COOK IS AVAILABLE
				//cooksAvailable.acquire();
				// GET NEXT ORDER FROM QUEUE
				Order order = Simulation.getOrder();
				while (order == null) {
					order = Simulation.getOrder();
				}
					
				
				// IF THERE IS AN ORDER TO MAKE
				if (order != null) {
					List<Food> orderList = order.order();
					ArrayList<Thread> machinesRunning = new ArrayList<Thread>();
					// RECEIVED AN ORDER
					// SIM EVENT
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, orderList, order.orderNum()));
					// CALL THE APPROPRIATE MACHINE DEPENDING ON FOOD TYPE
					for (Food item: orderList) {
						// SIM EVENT
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, item, order.orderNum()));
						if (item.name.equals("fries")) {
							// MAKE FRIES
							
							//synchronized(Simulation.fryers) {
							machinesRunning.add(Simulation.fryers.makeFood());
							//}
						} else if (item.name.equals("subs")) {
							// MAKE SUBS
							
							//synchronized(Simulation.presses) { 
							machinesRunning.add(Simulation.presses.makeFood());
							//}
						} else if (item.name.equals("pizza")) {
							// MAKE PIZZA
							
							//synchronized(Simulation.ovens) { 
							machinesRunning.add(Simulation.ovens.makeFood());
							//}
						} else if (item.name.equals("sodas")) {
							// MAKE SODA
							
							//synchronized(Simulation.sodaMachines) {
							machinesRunning.add(Simulation.sodaMachines.makeFood());
							//}
						}
						
						// SIM EVENT
					}
					for (Thread t: machinesRunning) {
						t.join();
					}
					for (Food item: orderList) {
						Simulation.logEvent(SimulationEvent.cookFinishedFood(this, item, order.orderNum()));
					}
					
					// SIM EVENT (OUTSIDE LOOP)
					Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, order.orderNum()));
					Simulation.orderDone(order);
					
					
				}
				// RELEASE LOCK
				//cooksAvailable.release();
			}
		} catch (InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			
			// I don't think I changed anything but we'll see if this breaks later
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}
