package code;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import code.Machines.MachineType;

/**
 * Simulation is the main class used to run the simulation. You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	public static LinkedList<Order> orders = new LinkedList<Order>();
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;
	// Machines
	public static Machines ovens;
	public static Machines fryers;
	public static Machines presses;
	public static Machines sodaMachines;
	// Order number of the last completed order
	private static ArrayList<Integer> ordersDone = new ArrayList<Integer>();;
	
	// Object to lock waiting customers/cooks onto
	public static Object customersWaiting = new Object();
	public static Object cooksWaiting = new Object();

		
	/**
	 * Used by other classes in the simulation to log events
	 * 
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}
	
	public static void submitOrder(List<Food> order, int orderNum) {
		synchronized(orders) {
			orders.add(new Order(order, orderNum));
			orders.notify();
		}
	}
	
	public static void orderDone(Order ord) {
		synchronized(customersWaiting) {
			ordersDone.add(ord.orderNum());
			customersWaiting.notifyAll();
		}
	}
	
	public static boolean orderUp(int orderNum) throws InterruptedException {
		synchronized(customersWaiting) {
			customersWaiting.wait();
		}
		return ordersDone.contains(orderNum);
	}

	public static Order getOrder() throws InterruptedException {
		synchronized(orders) {
			while (orders == null || orders.isEmpty()) {
				orders.wait();
			}
			
			return orders.removeLast();
		}
	}
	
	/**
	 * Function responsible for performing the simulation. Returns a List of
	 * SimulationEvent objects, constructed any way you see fit. This List will
	 * be validated by a call to Validate.validateSimulation. This method is
	 * called from Simulation.main(). We should be able to test your code by
	 * only calling runSimulation.
	 * 
	 * Parameters:
	 * 
	 * @param numCustomers the number of customers wanting to enter the restaurant
	 * @param numCooks the number of cooks in the simulation
	 * @param numTables the number of tables in the restaurant
	 * @param machineCount the count of all machine types in the restaurant
	 * @param randomOrders a flag say whether or not to give each customer a random
	 *        order
	 */
	public static List<SimulationEvent> runSimulation(
		int numCustomers, int numCooks,
		int numTables, int machineCount,
		boolean randomOrders) {

		// This method's signature MUST NOT CHANGE.

		/*
		 * We are providing this events list object for you.
		 * It is the ONLY PLACE where a concurrent collection object is
		 * allowed to be used.
		 */
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());


		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers,
			numCooks,
			numTables,
			machineCount));

		// Set things up you might need
		
		// SEMAPHORE FOR TABLES
		Semaphore sem = new Semaphore(numTables, true);
		// SEMAPHORE FOR COOKS
		Semaphore cooksAvailable = new Semaphore(numCooks, true);

		// FOOD TYPES
		Food pizza = new Food("pizza", 90);
		Food fries = new Food("fries", 30);
		Food subs = new Food("subs", 50);
		Food soda = new Food("soda", 2);


		// Start up machines
		ovens = new Machines(MachineType.ovens, pizza, machineCount);
		fryers = new Machines(MachineType.fryers, fries, machineCount);
		presses = new Machines(MachineType.grillPresses, subs, machineCount);
		sodaMachines = new Machines(MachineType.sodaMachines, soda, machineCount);
		
		// Simulation Events for Machines
		logEvent(SimulationEvent.machinesStarting(ovens, pizza, machineCount));
		logEvent(SimulationEvent.machinesStarting(fryers, fries, machineCount));
		logEvent(SimulationEvent.machinesStarting(presses, subs, machineCount));
		logEvent(SimulationEvent.machinesStarting(sodaMachines, soda, machineCount));


		// Let cooks in
		Thread[] cookThreads = new Thread[numCooks];
		for (int i = 0; i < cookThreads.length; i++) {
			cookThreads[i] = new Thread(new Cook("Cook " + i, cooksAvailable));
		}
		// Start all cook threads
		for (int i = 0; i < cookThreads.length; i++) {
			cookThreads[i].start();
		}


		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			order = new LinkedList<Food>();
			order.add(FoodType.fries);
			order.add(FoodType.pizza);
			order.add(FoodType.subs);
			order.add(FoodType.soda);
			for (int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(new Customer("Customer " + (i), order, sem));
			}
		} else {
			for (int i = 0; i < customers.length; i++) {
				Random rnd = new Random();
				int wingsCount = rnd.nextInt(4);
				int pizzaCount = rnd.nextInt(4);
				int subCount = rnd.nextInt(4);
				int sodaCount = rnd.nextInt(4);
				order = new LinkedList<Food>();
				for (int b = 0; b < wingsCount; b++) {
					order.add(FoodType.fries);
				}
				for (int f = 0; f < pizzaCount; f++) {
					order.add(FoodType.pizza);
				}
				for (int f = 0; f < subCount; f++) {
					order.add(FoodType.subs);
				}
				for (int c = 0; c < sodaCount; c++) {
					order.add(FoodType.soda);
				}
				customers[i] = new Thread(
					new Customer("Customer " + (i), order, sem));
			}
		}

		/*
		 * Now "let the customers know the shop is open" by starting them running in
		 * their own thread.
		 */
		for (int i = 0; i < customers.length; i++) {
			customers[i].start();
			/*
			 * NOTE: Starting the customer does NOT mean they get to go right into the shop.
			 * There has to be a table for them. The Customer class' run method has many
			 * jobs to do - one of these is waiting for an available table...
			 */
		}

		try {
			/*
			 * Wait for customers to finish
			 * -- you need to add some code here...
			 */
			for (int i = 0; i < customers.length; i++) {
				customers[i].join();
			}




			/*
			 * Then send cooks home...
			 * The easiest way to do this might be the following, where we interrupt their
			 * threads. There are other approaches though, so you can change this if you
			 * want to.
			 */
			for (int i = 0; i < cookThreads.length; i++)
				cookThreads[i].interrupt();
			for (int i = 0; i < cookThreads.length; i++)
				cookThreads[i].join();

		} catch (InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		// SIMULATION EVENT HERE



		logEvent(SimulationEvent.machinesEnding(ovens));
		logEvent(SimulationEvent.machinesEnding(fryers));
		logEvent(SimulationEvent.machinesEnding(presses));
		logEvent(SimulationEvent.machinesEnding(sodaMachines));

		// Done with simulation
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation. There
	 *        should be exactly four arguments: the first is the number of
	 *        customers, the second is the number of cooks, the third is the number
	 *        of tables in the restaurant, and the fourth is the number of items
	 *        each set of machines can make at the same time.
	 */
	public static void main(String args[]) throws InterruptedException {
		// Command line argument parser

// @formatter:off
/*
 * 		if (args.length != 4) {
 * 			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <count> <randomorders");
 * 			System.exit(1);
 * 		}
 * 		int numCustomers = new Integer(args[0]).intValue();
 * 		int numCooks = new Integer(args[1]).intValue();
 * 		int numTables = new Integer(args[2]).intValue();
 * 		int machineCount = new Integer(args[3]).intValue();
 * 		boolean randomOrders = new Boolean(args[4]);
 */
// @formatter: on
		
		// Parameters to the simulation
		int numCustomers = 20;
		int numCooks = 3;
		int numTables = 4;
		int machineCount = 4;
		boolean randomOrders = true;
		

		/* Run the simulation and then feed the result into the method to validate simulation. */
		System.out.println("Did it work? " +
			Validate.validateSimulation(
				runSimulation(
					numCustomers, numCooks,
					numTables, machineCount,
					randomOrders)));
	}

}
