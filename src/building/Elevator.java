package building;

import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Elevator.
 *
 * @author This class will represent an elevator, and will contain configuration
 *         information (capacity, speed, etc) as well as state information -
 *         such as stopped, direction, and count of passengers targeting each
 *         floor...
 */
public class Elevator {
	
	/**  Elevator State Variables - These are visible publicly. */
	public final static int STOP = 0;
	
	/** The Constant MVTOFLR. */
	public final static int MVTOFLR = 1;
	
	/** The Constant OPENDR. */
	public final static int OPENDR = 2;
	
	/** The Constant OFFLD. */
	public final static int OFFLD = 3;
	
	/** The Constant BOARD. */
	public final static int BOARD = 4;
	
	/** The Constant CLOSEDR. */
	public final static int CLOSEDR = 5;
	
	/** The Constant MV1FLR. */
	public final static int MV1FLR = 6;
	
	/** The Constant DOOR_CLOSED. */
	final static int DOOR_CLOSED = 0;
	
	/** The Constant DOOR_OPEN. */
	final static int DOOR_OPEN = 1;
	
	/** The Constant DOOR_MOVING. */
	final static int DOOR_MOVING = 2;

	/**
	 * Default configuration parameters for the elevator. These should be updated in
	 * the constructor.
	 */
	private int capacity = 15; // The number of PEOPLE the elevator can hold

	/** The ticks per floor. */
	private int ticksPerFloor = 5; // The time it takes the elevator to move between floors
	
	/** The ticks door open close. */
	private int ticksDoorOpenClose = 2; // The time it takes for doors to go from OPEN <=> CLOSED
	
	/** The pass per tick. */
	private int passPerTick = 3; // The number of PEOPLE that can enter/exit the elevator per tick

	/**  Finite State Machine State Variables. */
	private int currState; // current state
	
	/** The prev state. */
	private int prevState; // prior state
	
	/** The prev floor. */
	private int prevFloor; // prior floor
	
	/** The curr floor. */
	private int currFloor; // current floor
	
	/** The direction. */
	private int direction; // direction the Elevator is traveling in.

	/** The time in state. */
	private int timeInState; // represents the time in a given state
								// reset on state entry, used to determine if
								// state has completed or if floor has changed
								// *not* used in all states

	/** The door state. */
								private int doorState; // used to model the state of the doors - OPEN, CLOSED
							// or moving

	/** The passengers. */
							private int passengers; // the number of people in the elevator
	
	/** The pass by floor. */
	private ArrayList<Passengers>[] passByFloor; // Passengers to exit on the corresponding floor

	/** The move to floor. */
	private int moveToFloor; // When exiting the STOP state, this is the floor to move to without
								// stopping.

	/** The post move to floor dir. */
								private int postMoveToFloorDir; // This is the direction that the elevator will travel AFTER reaching
									// the moveToFloor in MVTOFLR state.
	
	/** The offload delay. */
									private int offloadDelay;
	
	/** The current onboarding groups. */
	private int currentOnboardingGroups;

	/**
	 * Instantiates a new elevator.
	 *
	 * @param numFloors the num floors
	 * @param capacity the capacity
	 * @param floorTicks the floor ticks
	 * @param doorTicks the door ticks
	 * @param passPerTick the pass per tick
	 */
	@SuppressWarnings("unchecked")
	public Elevator(int numFloors, int capacity, int floorTicks, int doorTicks, int passPerTick) {
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
		this.currFloor = 0;
		passByFloor = new ArrayList[numFloors];
		for (int i = 0; i < numFloors; i++)
			passByFloor[i] = new ArrayList<Passengers>();

		this.capacity = capacity;
		this.ticksPerFloor = floorTicks;
		this.ticksDoorOpenClose = doorTicks;
		this.passPerTick = passPerTick;
		this.doorState = DOOR_CLOSED;
		// TODO: Finish this constructor, adding configuration initialiation and
		// initialization of any other private fields, etc.
		offloadDelay = 0;
	}
	
	/**
	 * Move elevator.
	 */
	// Reviewed by Tohar Markovich
	public void moveElevator() {
		timeInState++;
		prevFloor = currFloor;
		if ((timeInState % ticksPerFloor) == 0) {
			currFloor = currFloor + direction;
		}
	}

	/**
	 * Update curr state.
	 *
	 * @param currState the curr state
	 */
	// Reviewed by Tohar Markovich
	public void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		if (this.prevState != this.currState) {
			timeInState = 0;
		}
	}
	
	/**
	 * Reset prev floor.
	 */
	public void resetPrevFloor() {
		prevFloor = currFloor;		
	}
	
	/**
	 * Checks if is stopped.
	 *
	 * @return true, if is stopped
	 */
	public boolean isStopped() {
		return currState == STOP;
	}
	
	/**
	 * Did state change.
	 *
	 * @return true, if successful
	 */
	public boolean didStateChange() {
		return prevState != currState || prevFloor != currFloor;
	}

	/**
	 * Gets the capacity.
	 *
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Sets the capacity.
	 *
	 * @param capacity the new capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Gets the ticks per floor.
	 *
	 * @return the ticks per floor
	 */
	public int getTicksPerFloor() {
		return ticksPerFloor;
	}

	/**
	 * Sets the ticks per floor.
	 *
	 * @param ticksPerFloor the new ticks per floor
	 */
	public void setTicksPerFloor(int ticksPerFloor) {
		this.ticksPerFloor = ticksPerFloor;
	}

	/**
	 * Gets the ticks door open close.
	 *
	 * @return the ticks door open close
	 */
	public int getTicksDoorOpenClose() {
		return ticksDoorOpenClose;
	}

	/**
	 * Sets the ticks door open close.
	 *
	 * @param ticksDoorOpenClose the new ticks door open close
	 */
	public void setTicksDoorOpenClose(int ticksDoorOpenClose) {
		this.ticksDoorOpenClose = ticksDoorOpenClose;
	}

	/**
	 * Gets the pass per tick.
	 *
	 * @return the pass per tick
	 */
	public int getPassPerTick() {
		return passPerTick;
	}

	/**
	 * Sets the pass per tick.
	 *
	 * @param passPerTick the new pass per tick
	 */
	public void setPassPerTick(int passPerTick) {
		this.passPerTick = passPerTick;
	}

	/**
	 * Gets the curr state.
	 *
	 * @return the curr state
	 */
	public int getCurrState() {
		return currState;
	}

	/**
	 * Sets the curr state.
	 *
	 * @param currState the new curr state
	 */
	public void setCurrState(int currState) {
		this.currState = currState;
	}

	/**
	 * Gets the prev state.
	 *
	 * @return the prev state
	 */
	public int getPrevState() {
		return prevState;
	}

	/**
	 * Sets the prev state.
	 *
	 * @param prevState the new prev state
	 */
	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}

	/**
	 * Gets the prev floor.
	 *
	 * @return the prev floor
	 */
	public int getPrevFloor() {
		return prevFloor;
	}

	/**
	 * Sets the prev floor.
	 *
	 * @param prevFloor the new prev floor
	 */
	public void setPrevFloor(int prevFloor) {
		this.prevFloor = prevFloor;
	}

	/**
	 * Gets the curr floor.
	 *
	 * @return the curr floor
	 */
	public int getCurrFloor() {
		return currFloor;
	}

	/**
	 * Sets the curr floor.
	 *
	 * @param currFloor the new curr floor
	 */
	public void setCurrFloor(int currFloor) {
		this.currFloor = currFloor;
	}

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Sets the direction.
	 *
	 * @param direction the new direction
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * Gets the time in state.
	 *
	 * @return the time in state
	 */
	public int getTimeInState() {
		return timeInState;
	}

	/**
	 * Sets the time in state.
	 *
	 * @param timeInState the new time in state
	 */
	public void setTimeInState(int timeInState) {
		this.timeInState = timeInState;
	}

	/**
	 * Gets the door state.
	 *
	 * @return the door state
	 */
	public int getDoorState() {
		return doorState;
	}

	/**
	 * Sets the door state.
	 *
	 * @param doorState the new door state
	 */
	public void setDoorState(int doorState) {
		this.doorState = doorState;
	}

	/**
	 * Gets the passengers.
	 *
	 * @return the passengers
	 */
	public int getPassengers() {
		return passengers;
	}

	/**
	 * Sets the passengers.
	 *
	 * @param passengers the new passengers
	 */
	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}

	/**
	 * Gets the pass by floor.
	 *
	 * @return the pass by floor
	 */
	// Reviewed by Tohar Markovich
	public ArrayList<Passengers>[] getPassByFloor() {
		return passByFloor;
	}

	/**
	 * Sets the pass by floor.
	 *
	 * @param passByFloor the new pass by floor
	 */
	// Reviewed by Tohar Markovich
	public void setPassByFloor(ArrayList<Passengers>[] passByFloor) {
		this.passByFloor = passByFloor;
	}

	/**
	 * Adds the passengers.
	 *
	 * @param p the p
	 */
	// Reviewed by Tohar Markovich
	public void addPassengers(Passengers p) {
		passByFloor[p.getDestFloor()].add(p);
		passengers += p.getNumPass();
	}
	
	/**
	 * Clear floor.
	 *
	 * @param floor the floor
	 */
	// Reviewed by Tohar Markovich
	public void clearFloor(int floor) {
		for (Passengers p : passByFloor[floor]) {
			passengers -= p.getNumPass();
		}
		passByFloor[floor].clear();
	}
	
	/**
	 * Gets the move to floor.
	 *
	 * @return the move to floor
	 */
	public int getMoveToFloor() {
		return moveToFloor;
	}

	/**
	 * Sets the move to floor.
	 *
	 * @param moveToFloor the new move to floor
	 */
	public void setMoveToFloor(int moveToFloor) {
		this.moveToFloor = moveToFloor;
	}

	/**
	 * Gets the post move to floor dir.
	 *
	 * @return the post move to floor dir
	 */
	public int getPostMoveToFloorDir() {
		return postMoveToFloorDir;
	}

	/**
	 * Sets the post move to floor dir.
	 *
	 * @param postMoveToFloorDir the new post move to floor dir
	 */
	public void setPostMoveToFloorDir(int postMoveToFloorDir) {
		this.postMoveToFloorDir = postMoveToFloorDir;
	}

	/**
	 * Gets the current onboarding groups.
	 *
	 * @return the current onboarding groups
	 */
	public int getCurrentOnboardingGroups() {
		return currentOnboardingGroups;
	}

	/**
	 * Sets the current onboarding groups.
	 *
	 * @param currentOnboardingGroups the new current onboarding groups
	 */
	public void setCurrentOnboardingGroups(int currentOnboardingGroups) {
		this.currentOnboardingGroups = currentOnboardingGroups;
	}

	/**
	 * Gets the offload delay.
	 *
	 * @return the offload delay
	 */
	public int getOffloadDelay() {
		return offloadDelay;
	}

	/**
	 * Sets the offload delay.
	 *
	 * @param offloadDelay the new offload delay
	 */
	public void setOffloadDelay(int offloadDelay) {
		this.offloadDelay = offloadDelay;
	}


	// TODO: Add Getter/Setters and any methods that you deem are required. Examples
	// include:
	// 1) moving the elevator
	// 2) closing the doors
	// 3) opening the doors
	// and so on...

}
