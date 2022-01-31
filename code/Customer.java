package code;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order. When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private final Semaphore tablesTaken;

	public static int runningCounter = -1;
	

	/**
	 * You can feel free modify this constructor. It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, Semaphore sem) {
		this.name = name;
		this.order = order;
		tablesTaken = sem;
		this.orderNum = ++runningCounter;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 */
	public void run() {
		// Simulation Event:
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		// Do the thing
		try {
			// Attempt to enter
			tablesTaken.acquire();
			// TODO -- FINISH ACTUAL CUSTOMER BEHAVIOR
			
			// ENTERED
			Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this));
			// ABOUT TO PLACE ORDER
			Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
			// TODO -- SEND ORDER TO COOKS AND FINISH RUN
			// SEND ORDER

			Simulation.submitOrder(order, orderNum);
				
			
			// WAIT FOR ORDER & SIM EVENT
			boolean orderDone = Simulation.orderUp(orderNum);
			while (!orderDone) {
				orderDone = Simulation.orderUp(orderNum);
			}
			Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
			
			// EAT ORDER
			// GO TO OFFICE HOURS ABOUT THIS PART!!!!!!!
			//System.out.println("CUSTOMER NOT DONE!!!");
			
			
		} catch (Exception e) {
			System.out.println("CAUGHT EXCEPTION: " + e);
		} finally {
			// RELEASE SEMAPHORE (and leave restaurant)
			Simulation.logEvent(SimulationEvent.customerLeavingRatsies(this));
			tablesTaken.release();
			// SIM EVENT
			
		}
		
	}
}
