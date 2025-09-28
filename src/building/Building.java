package building;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import myfileio.MyFileIO;
import genericqueue.GenericQueue;

// TODO: Auto-generated Javadoc
/**
 * The Class Building.
 * 
 * @author tohar
 */
// TODO: Auto-generated Javadoc
public class Building {

	/** Constants for direction. */
	final static int UP = 1;

	/** The Constant DOWN. */
	final static int DOWN = -1;

	/** The Constant LOGGER. */
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());

	/** The fh - used by LOGGER to write the log messages to a file. */
	private FileHandler fh;

	/** The fio for writing necessary files for data analysis. */
	private MyFileIO fio;

	/** File that will receive the information for data analysis. */
	private File passDataFile;

	/** passSuccess holds all Passengers who arrived at their destination floor. */
	private ArrayList<Passengers> passSuccess;

	/** gaveUp holds all Passengers who gave up and did not use the elevator. */
	private ArrayList<Passengers> gaveUp;

	/** The number of floors - must be initialized in constructor. */
	private final int NUM_FLOORS;

	/** The size of the up/down queues on each floor. */
	private final int FLOOR_QSIZE = 10;

	/**
	 * passQ holds the time-ordered queue of Passengers, initialized at the start of
	 * the simulation. At the end of the simulation, the queue will be empty.
	 */
	private GenericQueue<Passengers> passQ;

	/** The size of the queue to store Passengers at the start of the simulation. */
	private final int PASSENGERS_QSIZE = 1000;

	/** The number of elevators - must be initialized in constructor. */
	private final int NUM_ELEVATORS;

	/** The floors. */
	public Floor[] floors;

	/** The elevators. */
	private Elevator[] elevators;

	/**
	 * The Call Manager - it tracks calls for the elevator, analyzes them to answer
	 * questions and prioritize calls.
	 */
	private CallManager callMgr;

	/** The prioritized for mv to flr. */
	private Passengers prioritizedForBoard;
	
	/* Passengers for the GUI */
	
	/** The arrival passengers. */
	private List<Passengers> arrivalPassengers;
	
	/** The boarding passengers. */
	private List<Passengers> boardingPassengers;
	
	/**  Should we end simulation. */
	private boolean endSim;
	
	/**
	 * Instantiates a new building.
	 *
	 * @param numFloors    the num floors
	 * @param numElevators the num elevators
	 * @param logfile      the logfile
	 */
	public Building(int numFloors, int numElevators, String logfile) {
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		passQ = new GenericQueue<Passengers>(PASSENGERS_QSIZE);
		passSuccess = new ArrayList<Passengers>();
		gaveUp = new ArrayList<Passengers>();
		arrivalPassengers = new ArrayList<Passengers>();
		boardingPassengers = new ArrayList<Passengers>();
		Passengers.resetStaticID();
		initializeBuildingLogger(logfile);
		// passDataFile is where you will write all the results for those passengers who
		// successfully
		// arrived at their destination and those who gave up...
		fio = new MyFileIO();
		passDataFile = fio.getFileHandle(logfile.replaceAll(".log", "PassData.csv"));

		// create the floors, call manager and the elevator arrays
		// note that YOU will need to create and config each specific elevator...
		floors = new Floor[NUM_FLOORS];
		for (int i = 0; i < NUM_FLOORS; i++) {
			floors[i] = new Floor(FLOOR_QSIZE);
		}
		callMgr = new CallManager(floors, NUM_FLOORS);
		elevators = new Elevator[NUM_ELEVATORS];
		// TODO: if you defined new fields, make sure to initialize them here
	}

	// TODO: Place all of your code HERE - state methods and helpers...
	
	/**
	 * Do a time step and return done status.
	 *
	 * @param stepCnt the current time
	 * @return true if done, false otherwise
	 */
	// Reviewed by Jack Fu
	public boolean step(int stepCnt) {
		checkPassengerArrival(stepCnt);
		checkPassengerGiveup(stepCnt);
		callMgr.updateCallStatus();
		updateElevator(stepCnt);
		if (!endSim) {
			endSim = passengersProcessed() && allElevatorsStopped();
			return false;
		}
		closeLogs(stepCnt);
		processPassengerData();
		return true;
	}
	
	/**
	 * Adds the passengers to queue.
	 *
	 * @param time      the time
	 * @param numPass   the num pass
	 * @param fromFloor the from floor
	 * @param toFloor   the to floor
	 * @param polite    the polite
	 * @param wait      the wait
	 * @return true, if successful
	 */
	// Reviewed by Jack Fu
	public boolean addPassengersToQueue(int time, int numPass, int fromFloor, int toFloor, boolean polite, int wait) {
		return passQ.add(new Passengers(time, numPass, fromFloor, toFloor, polite, wait));
	}

	/**
	 * Config elevators.
	 *
	 * @param numFloors   the num floors
	 * @param capacity    the capacity
	 * @param floorTicks  the floor ticks
	 * @param doorTicks   the door ticks
	 * @param passPerTick the pass per tick
	 */
	// Reviewed by Jack Fu
	public void configElevators(int numFloors, int capacity, int floorTicks, int doorTicks, int passPerTick) {
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i] = new Elevator(numFloors, capacity, floorTicks, doorTicks, passPerTick);
		}
	}

	/**
	 * Helper methods (for Controller as well).
	 *
	 * @return true, if successful
	 */

	/**
	 * Checks if passengers were processed.
	 *
	 * @return true, if successful
	 */
	// Reviewed by Jack Fu
	public boolean passengersProcessed() {
		if (!passQ.isEmpty()) {
			return false;
		}
		for (Floor f : floors) {
			if (!f.goingDownEmpty()) {
				return false;
			}
			if (!f.goingUpEmpty()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Check passenger arrival.
	 *
	 * @param time the time
	 * @return true, if successful
	 */
	// Reviewed by Jack Fu
	public void checkPassengerArrival(int time) {
		Passengers p = passQ.peek();
		while (p != null && time == p.getTime()) {
			if (p.getDirection() == UP) {
				floors[p.getOnFloor()].addToUp(p);
			} else {
				floors[p.getOnFloor()].addToDown(p);
			}
			logCalls(time, p.getNumPass(), p.getOnFloor(), p.getDirection(), p.getId());
			arrivalPassengers.add(p);
			passQ.remove();
			p = passQ.peek();
		}
	}

	/**
	 * Check passenger giveup.
	 *
	 * @param time the time
	 * @return true, if successful
	 */
	// Reviewed by Jack Fu
	public void checkPassengerGiveup(int time) {
		for (Floor f : floors) {
			Passengers p = f.peekFromUp();
			if (p != null && p.getTimeWillGiveUp() + 1 == time) {
				gaveUp.add(p);
				f.pollFromUp();
			}
			p = f.peekFromDown();
			if (p != null && p.getTimeWillGiveUp() + 1 == time) {
				gaveUp.add(p);
				f.pollFromDown();
			}
		}
	}

	/**
	 * Arrival passengers.
	 *
	 * @return the passengers[]
	 */
	// Reviewed by Jack Fu
	public Passengers[] arrivalPassengers() {
		Passengers[] p = arrivalPassengers.toArray(new Passengers[arrivalPassengers.size()]);
		arrivalPassengers.clear();
		return p;
	}
	
	/**
	 * Boarding passengers.
	 *
	 * @return the passengers[]
	 */
	// Reviewed by Jack Fu
	public Passengers[] boardingPassengers() {
		Passengers[] p = boardingPassengers.toArray(new Passengers[boardingPassengers.size()]);
		boardingPassengers.clear();
		return p;
	}
	
	/**
	 * Give up passengers.
	 *
	 * @return the passengers[]
	 */
	// Reviewed by Jack Fu
	public Passengers[] giveUpPassengers() {
		Passengers[] p = gaveUp.toArray(new Passengers[gaveUp.size()]);
		return p;
	}
	
	/**
	 * Gets the curr num pass.
	 *
	 * @return the curr num pass
	 */
	// Reviewed by Jack Fu
	public int getCurrNumPass() {
		return elevators[0].getPassengers();
	}
	
	/**
	 * Gets the curr direction.
	 *
	 * @return the curr direction
	 */
	// Reviewed by Jack Fu
	public int getCurrDirection() {
		return elevators[0].getDirection();
	}
		
	/**
	 * State Machine.
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */

	/**
	 * Stop state. If there are no calls in any direction - @STOP If there is a call
	 * down or up - @OPENDR If there are no calls on this floor, but there are calls
	 * on other floors - @MVTOFLOOR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	public int currStateStop(int time, Elevator lift) {
		lift.setTimeInState(lift.getTimeInState() + 1);
		if (!callMgr.isUpCallPending() && !callMgr.isDownCallPending()) {
			return Elevator.STOP;
		} else {
			int floor = lift.getCurrFloor();
			Passengers p = callMgr.prioritizePassengerCalls(lift);
			prioritizedForBoard = p;
			if (p.getOnFloor() == floor) {
				lift.setDirection(p.getDirection());
				return Elevator.OPENDR;
			} else {
				lift.setDirection(p.getOnFloor() > lift.getCurrFloor() ? UP : DOWN);
				lift.setMoveToFloor(p.getOnFloor());
				lift.setPostMoveToFloorDir(p.getDirection());
				return Elevator.MVTOFLR;
			}
		}
	}

	/**
	 * Close dr state. Closes the elevator doors, decrements door state variable
	 * Passengers arrive on current floor in same direction and are NOT polite
	 * - @OPENDR Doors are not closed yet - @CLOSEDR Elevator is empty and no calls
	 * in any direction - @STOP There are passengers in the elevator to get off on
	 * other floors in the current direction or there are calls on floors moving in
	 * the current direction waiting to be serviced. Elevator direction could change
	 * if no calls on floors moving in curr direction, but there are calls on floors
	 * moving in opp direction - @MV1FLR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	protected int currStateCloseDr(int time, Elevator lift) {
		int direction = lift.getDirection();
		Passengers pInCurrDir = direction == UP ? floors[lift.getCurrFloor()].peekFromUp()
				: floors[lift.getCurrFloor()].peekFromDown();
		if (pInCurrDir != null && !pInCurrDir.isPolite()) {
			pInCurrDir.setPolite(true);
			return Elevator.OPENDR;
		}
		lift.setTimeInState(lift.getTimeInState() + 1);
		if (lift.getTimeInState() < lift.getTicksDoorOpenClose()) {
			lift.setDoorState(Elevator.DOOR_MOVING);
			return Elevator.CLOSEDR;
		} else {
			lift.setDoorState(Elevator.DOOR_CLOSED);
			if (lift.getPassengers() == 0) {
				return elevatorEmptyClosed(lift.getDirection(), lift);
			} else {
				return Elevator.MV1FLR;
			}
		}
	}

	/**
	 * Elevator empty. Helper method
	 * 
	 * @param direction the direction
	 * @param lift      the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	private int elevatorEmptyClosed(int direction, Elevator lift) {
		if (!callMgr.isDownCallPending() && !callMgr.isUpCallPending()) {
			return Elevator.STOP;
		}
		int floor = lift.getCurrFloor();
		if (direction == UP && callMgr.numCallsPendingAbove(floor) > 0 || direction == DOWN && callMgr.numCallsPendingBelow(floor) > 0) {
			return Elevator.MV1FLR;
		} else {
			if (direction == UP && !floors[floor].goingUpEmpty()
					|| direction == DOWN && !floors[floor].goingDownEmpty()) {
				return Elevator.OPENDR;
			}
			direction = direction * -1;
			lift.setDirection(direction);
			if (direction == UP && !floors[floor].goingUpEmpty()
					|| direction == DOWN && !floors[floor].goingDownEmpty()) {
				return Elevator.OPENDR;
			} else {
				return Elevator.MV1FLR;
			}
		}
	}

	/**
	 * Board state. Boards all waiting passengers in current direction Based upon
	 * number of boarders, wait time needs to continually be re-evaluated, since new
	 * boarders can arrive Floor queue needs to be examined for new arrivals every
	 * tick in this state There is capacity in the elevator and not enough time has
	 * passed to board all waiting passengers - @BOARD All passengers have boarded
	 * or no room for more - @CLOSEDR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	protected int currStateBoard(int time, Elevator lift) {
		lift.setTimeInState(lift.getTimeInState() + 1);
		if (prioritizedForBoard != null) {
			lift.setDirection(prioritizedForBoard.getDirection());
			prioritizedForBoard = null;
		}
		GenericQueue<Passengers> q = lift.getDirection() == UP ?
				floors[lift.getCurrFloor()].getUpQueue() :
					floors[lift.getCurrFloor()].getDownQueue();
		Passengers p = q.peek();
		loopOverBoarding(time, lift, p, q);
		
		int delay = (lift.getCurrentOnboardingGroups() + lift.getPassPerTick() - 1) / lift.getPassPerTick();
		if (lift.getTimeInState() >= delay) {
			lift.setCurrentOnboardingGroups(0);
			if (p != null) {
				p.setLoggedSkip(false);
			}
			return Elevator.CLOSEDR;
		}
		return Elevator.BOARD;		
	}
	
	/**
	 * Loop over boarding.
	 *
	 * @param time the time
	 * @param lift the lift
	 * @param p the p
	 * @param q the q
	 */
	// Reviewed by Jack Fu
	public void loopOverBoarding(int time, Elevator lift, Passengers p, GenericQueue<Passengers> q) {
		while (p != null) {
			if (p.getTimeWillGiveUp() + 1 == time) {
				q.poll();
				gaveUp.add(p);
				logGiveUp(time, p.getNumPass(), lift.getCurrFloor(), lift.getDirection(), p.getId());
			} else if (p.getNumPass() + lift.getPassengers() > lift.getCapacity()) {
				if (!p.isLoggedSkip()) {
					logSkip(time, p.getNumPass(), lift.getCurrFloor(), p.getDirection(), p.getId());
					p.setLoggedSkip(true);
					p.setPolite(true);					
				}
				break;
			} else {
				lift.setCurrentOnboardingGroups(lift.getCurrentOnboardingGroups() + p.getNumPass());
				p.setBoardTime(time);
				logBoard(time, p.getNumPass(), lift.getCurrFloor(), p.getDirection(), p.getId());
				q.poll();
				lift.addPassengers(p);
				boardingPassengers.add(p);
			}
			p = q.peek();
		}
	}

	/**
	 * Open dr state. Opens the door for off-loading or boarding Opens the door,
	 * increments door state variable If the doors are not fully open - @OPENDR If
	 * the doors are open and there are passengers waiting to get off - @OFFLD If
	 * the doors are open, no passengers getting off, and passengers want to get on
	 * in the current direction - @BOARD
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	protected int currStateOpenDr(int time, Elevator lift) {
		lift.resetPrevFloor();			
		lift.setTimeInState(lift.getTimeInState() + 1);
		if (lift.getTimeInState() < lift.getTicksDoorOpenClose()) {
			lift.setDoorState(Elevator.DOOR_MOVING);
			return Elevator.OPENDR;
		} else {
			lift.setDoorState(Elevator.DOOR_OPEN);
			int floor = lift.getCurrFloor();
			List<Passengers> pToExit = lift.getPassByFloor()[floor];
			if (!pToExit.isEmpty()) {
				return Elevator.OFFLD;
			}
			int dir = lift.getDirection();
			if (!floors[floor].goingDownEmpty() && dir == DOWN) {
				return Elevator.BOARD;
			}
			if (!floors[floor].goingUpEmpty() && dir == UP) {
				return Elevator.BOARD;
			}				
			return Elevator.CLOSEDR;
		}
	}

	/**
	 * Mvto flr state. Move the elevator Once it reaches the target floor, change
	 * the elevator direction If the elevator has not reached the target floor
	 * - @MVTOFLR If the elevator has reached the target floor - @OPENDR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	protected int currStateMvToFlr(int time, Elevator lift) {
		lift.moveElevator();
		if (lift.getCurrFloor() == prioritizedForBoard.getOnFloor()) {
			lift.setDirection(prioritizedForBoard.getDirection());
			return Elevator.OPENDR;
		} else {
			return Elevator.MVTOFLR;
		}
	}

	/**
	 * Off ld state. Models the time for passengers to leave the elevator Rate is
	 * specified by passPerTick Passengers leave the elevator, all passengers are
	 * assumed to leave in the first cycle in this state, May change directions in
	 * this state after passengers leave Not enought ime has passed to allow all
	 * passengers to exit - @OFFLD All passengers have exited, passengers want to
	 * board - @BOARD All passengers have exited, no passengers to board - @CLOSEDR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 * @todo Keep working on this. Might not be fully correct. getTimeArrived() time
	 *       to offload?
	 */
	// Reviewed by Jack Fu
	protected int currStateOffLd(int time, Elevator lift) {
		int timeInState = lift.getTimeInState();
		lift.setTimeInState(timeInState + 1);
		if (lift.getPrevState() != Elevator.OFFLD) {
			List<Passengers> passengers = lift.getPassByFloor()[lift.getCurrFloor()];
			int totalPassengers = 0;
			for (Passengers p : passengers) {
				totalPassengers += p.getNumPass();
				p.setTimeArrived(time);
				logArrival(time, p.getNumPass(), lift.getCurrFloor(), p.getId());
			}
			lift.setOffloadDelay((totalPassengers + lift.getPassPerTick() - 1) / lift.getPassPerTick());
			passSuccess.addAll(passengers);
			lift.clearFloor(lift.getCurrFloor());
		}
		if (lift.getTimeInState() == lift.getOffloadDelay()) {
			lift.setOffloadDelay(0);
			return timeInStateEqualsOffldDelay(lift);
		}
		return Elevator.OFFLD;
	}

	/**
	 * Time in state equals offld delay. Helper method
	 * 
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	private int timeInStateEqualsOffldDelay(Elevator lift) {
		if (lift.getDirection() == DOWN && !floors[lift.getCurrFloor()].goingDownEmpty()) {
			return Elevator.BOARD;
		}
		if (lift.getDirection() == UP && !floors[lift.getCurrFloor()].goingUpEmpty()) {
			return Elevator.BOARD;
		}
		if (lift.getPassengers() == 0) {
			if (lift.getDirection() == UP && callMgr.numCallsPendingAbove(lift.getCurrFloor()) == 0) {
				if (!floors[lift.getCurrFloor()].goingDownEmpty()) {
					lift.setDirection(DOWN);
					return Elevator.BOARD;
				}
			}
			if (lift.getDirection() == DOWN && callMgr.numCallsPendingBelow(lift.getCurrFloor()) == 0) {
				if (!floors[lift.getCurrFloor()].goingUpEmpty()) {
					lift.setDirection(UP);
					return Elevator.BOARD;
				}
			}
		}
		return Elevator.CLOSEDR;
	}

	/**
	 * Mv 1 flr state. Move the elevator to the next floor in the current direction
	 * In between floors or no passengers to exit or board at the new floor
	 * - @MV1FLR Reached the new floor and there are either passengers to exit or
	 * passengers to board in the same direction. Also possible that direction could
	 * change - @OPENDR
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	protected int currStateMv1Flr(int time, Elevator lift) {
		lift.moveElevator();
		int floor = lift.getCurrFloor();
		if (lift.getPrevFloor() != floor) {
			if (lift.getPassByFloor()[floor].size() > 0) {
				return Elevator.OPENDR;
			}
			if (lift.getDirection() == DOWN && !floors[floor].goingDownEmpty()) {
				return Elevator.OPENDR;
			}
			if (lift.getDirection() == UP && !floors[floor].goingUpEmpty()) {
				return Elevator.OPENDR;
			}
			if (lift.getPassengers() == 0) {
				return elevatorEmpty(lift);
			}
		}
		return Elevator.MV1FLR;
	}

	/**
	 * Elevator empty. Helper method
	 * 
	 * @param lift the lift
	 * @return the int
	 */
	// Reviewed by Jack Fu
	private int elevatorEmpty(Elevator lift) {
		int floor = lift.getCurrFloor();
		int dir = lift.getDirection();
		if (dir == UP && callMgr.numCallsPendingAbove(floor) == 0) {
			if (!floors[floor].goingDownEmpty()) {
				lift.setDirection(DOWN);
				return Elevator.OPENDR;
			}
		}
		if (dir == DOWN && callMgr.numCallsPendingBelow(floor) == 0) {
			if (!floors[floor].goingUpEmpty()) {
				lift.setDirection(UP);
				return Elevator.OPENDR;
			}
		}
		return Elevator.MV1FLR;
	}

	/**
	 * All elevators stopped.
	 *
	 * @return true, if successful
	 */
	// Reviewed by Jack Fu
	public boolean allElevatorsStopped() {
		for (Elevator e : elevators) {
			if (!e.isStopped()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the current state. This is a hacky solution. If more elevators were
	 * added, this would not work.
	 * 
	 * @return the current state
	 */
	// Reviewed by Jack Fu
	public int getCurrentState() {
		return elevators[0].getCurrState();
	}

	/**
	 * Gets the current floor. This is a hacky solution. If more elevators were
	 * added, this would not work.
	 * 
	 * @return the current floor
	 */
	// Reviewed by Jack Fu
	public int getCurrentFloor() {
		return elevators[0].getCurrFloor();
	}

	/**
	 * Gets the elevator.
	 *
	 * @return the elevator
	 */
	// Reviewed by Jack Fu
	public Elevator getElevator() {
		return elevators[0];
	}

	// DO NOT CHANGE ANYTHING BELOW THIS LINE:
	/**
	 * Initialize building logger. Sets formating, file to log to, and turns the
	 * logger OFF by default
	 *
	 * @param logfile the file to log information to
	 */
	void initializeBuildingLogger(String logfile) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$-7s %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update elevator - this is called AFTER time has been incremented. - Logs any
	 * state changes, if the have occurred, - Calls appropriate method based upon
	 * currState to perform any actions and calculate next state...
	 *
	 * @param time the time
	 */
	// YOU WILL NEED TO CODE ANY MISSING METHODS IN THE APPROPRIATE CLASSES...
	public void updateElevator(int time) {
		for (Elevator lift : elevators) {
			if (lift.didStateChange()) {
				logElevatorStateChanged(time, lift.getPrevState(), lift.getCurrState(), lift.getPrevFloor(),
					lift.getCurrFloor());				
			}
			switch (lift.getCurrState()) {
			case Elevator.STOP:
				lift.updateCurrState(currStateStop(time, lift));
				break;
			case Elevator.MVTOFLR:
				lift.updateCurrState(currStateMvToFlr(time, lift));
				break;
			case Elevator.OPENDR:
				lift.updateCurrState(currStateOpenDr(time, lift));
				break;
			case Elevator.OFFLD:
				lift.updateCurrState(currStateOffLd(time, lift));
				break;
			case Elevator.BOARD:
				lift.updateCurrState(currStateBoard(time, lift));
				break;
			case Elevator.CLOSEDR:
				lift.updateCurrState(currStateCloseDr(time, lift));
				break;
			case Elevator.MV1FLR:
				lift.updateCurrState(currStateMv1Flr(time, lift));
				break;
			}
		}
	}

	/**
	 * Process passenger data. Do NOT change this - it simply dumps the collected
	 * passenger data for successful arrivals and give ups. These are assumed to be
	 * ArrayLists...
	 */
	public void processPassengerData() {
		try {
			BufferedWriter out = fio.openBufferedWriter(passDataFile);
			out.write("ID,Number,From,To,WaitToBoard,TotalTime\n");
			for (Passengers p : passSuccess) {
				String str = p.getId() + "," + p.getNumPass() + "," + (p.getOnFloor() + 1) + ","
						+ (p.getDestFloor() + 1) + "," + (p.getBoardTime() - p.getTime()) + ","
						+ (p.getTimeArrived() - p.getTime()) + "\n";
				out.write(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId() + "," + p.getNumPass() + "," + (p.getOnFloor() + 1) + ","
						+ (p.getDestFloor() + 1) + "," + p.getWaitTime() + ",-1\n";
				out.write(str);
			}
			fio.closeFile(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enable logging. Prints the initial configuration message. For testing,
	 * logging must be enabled BEFORE the run starts.
	 */
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
		for (Elevator el : elevators)
			logElevatorConfig(el.getCapacity(), el.getTicksPerFloor(), el.getTicksDoorOpenClose(), el.getPassPerTick(),
					el.getCurrState(), el.getCurrFloor());
	}

	/**
	 * Close logs, and pause the timeline in the GUI.
	 *
	 * @param time the time
	 */
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			logEndSimulation(time);
			fh.flush();
			fh.close();
		}
	}

	/**
	 * Prints the state.
	 *
	 * @param state the state
	 * @return the string
	 */
	private String printState(int state) {
		String str = "";

		switch (state) {
		case Elevator.STOP:
			str = "STOP   ";
			break;
		case Elevator.MVTOFLR:
			str = "MVTOFLR";
			break;
		case Elevator.OPENDR:
			str = "OPENDR ";
			break;
		case Elevator.CLOSEDR:
			str = "CLOSEDR";
			break;
		case Elevator.BOARD:
			str = "BOARD  ";
			break;
		case Elevator.OFFLD:
			str = "OFFLD  ";
			break;
		case Elevator.MV1FLR:
			str = "MV1FLR ";
			break;
		default:
			str = "UNDEF  ";
			break;
		}
		return (str);
	}

	/**
	 * Dump passQ contents. Debug hook to view the contents of the passenger
	 * queue...
	 */
	public void dumpPassQ() {
		ListIterator<Passengers> passengers = passQ.getListIterator();
		if (passengers != null) {
			System.out.println("Passengers Queue:");
			while (passengers.hasNext()) {
				Passengers p = passengers.next();
				System.out.println(p);
			}
		}
	}

	/**
	 * Logging.
	 *
	 * @param capacity           the capacity
	 * @param ticksPerFloor      the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick        the pass per tick
	 * @param state              the state
	 * @param floor              the floor
	 */

	/**
	 * Log elevator config.
	 *
	 * @param capacity           the capacity
	 * @param ticksPerFloor      the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick        the pass per tick
	 * @param state              the state
	 * @param floor              the floor
	 */
	private void logElevatorConfig(int capacity, int ticksPerFloor, int ticksDoorOpenClose, int passPerTick, int state,
			int floor) {
		LOGGER.info("CONFIG:   Capacity=" + capacity + "   Ticks-Floor=" + ticksPerFloor + "   Ticks-Door="
				+ ticksDoorOpenClose + "   Ticks-Passengers=" + passPerTick + "   CurrState=" + (printState(state))
				+ "   CurrFloor=" + (floor + 1));
	}

	/**
	 * Log elevator state changed.
	 *
	 * @param time      the time
	 * @param prevState the prev state
	 * @param currState the curr state
	 * @param prevFloor the prev floor
	 * @param currFloor the curr floor
	 */
	private void logElevatorStateChanged(int time, int prevState, int currState, int prevFloor, int currFloor) {
		LOGGER.info("Time=" + time + "   Prev State: " + printState(prevState) + "   Curr State: "
				+ printState(currState) + "   PrevFloor: " + (prevFloor + 1) + "   CurrFloor: " + (currFloor + 1));
	}

	/**
	 * Log arrival.
	 *
	 * @param time    the time
	 * @param numPass the num pass
	 * @param floor   the floor
	 * @param id      the id
	 */
	private void logArrival(int time, int numPass, int floor, int id) {
		LOGGER.info("Time=" + time + "   Arrived=" + numPass + " Floor=" + (floor + 1) + " passID=" + id);
	}

	/**
	 * Log calls.
	 *
	 * @param time    the time
	 * @param numPass the num pass
	 * @param floor   the floor
	 * @param dir     the dir
	 * @param id      the id
	 */
	private void logCalls(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time=" + time + "   Called=" + numPass + " Floor=" + (floor + 1) + " Dir="
				+ ((dir > 0) ? "Up" : "Down") + "   passID=" + id);
	}

	/**
	 * Log give up.
	 *
	 * @param time    the time
	 * @param numPass the num pass
	 * @param floor   the floor
	 * @param dir     the dir
	 * @param id      the id
	 */
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time=" + time + "   GaveUp=" + numPass + " Floor=" + (floor + 1) + " Dir="
				+ ((dir > 0) ? "Up" : "Down") + "   passID=" + id);
	}

	/**
	 * Log skip.
	 *
	 * @param time    the time
	 * @param numPass the num pass
	 * @param floor   the floor
	 * @param dir     the dir
	 * @param id      the id
	 */
	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time=" + time + "   Skip=" + numPass + " Floor=" + (floor + 1) + " Dir="
				+ ((dir > 0) ? "Up" : "Down") + "   passID=" + id);
	}

	/**
	 * Log board.
	 *
	 * @param time    the time
	 * @param numPass the num pass
	 * @param floor   the floor
	 * @param dir     the dir
	 * @param id      the id
	 */
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time=" + time + "   Board=" + numPass + " Floor=" + (floor + 1) + " Dir="
				+ ((dir > 0) ? "Up" : "Down") + "   passID=" + id);
	}

	/**
	 * Log end simulation.
	 *
	 * @param time the time
	 */
	private void logEndSimulation(int time) {
		LOGGER.info("Time=" + time + "   Detected End of Simulation");
	}
}
