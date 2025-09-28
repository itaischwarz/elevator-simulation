package building;

// TODO: Auto-generated Javadoc
/**
 * The Class CallManager. This class models all of the calls on each floor, and
 * then provides methods that allow the building to determine what needs to
 * happen (ie, state transitions).
 * 
 * @author tohar
 */
public class CallManager {

	/** The floors. */
	private Floor[] floors;

	/** The num floors. */
	private final int NUM_FLOORS;

	/** The Constant UP. */
	private final static int UP = 1;

	/** The Constant DOWN. */
	private final static int DOWN = -1;

	/**
	 * The up calls array indicates whether or not there is a up call on each floor.
	 */
	private boolean[] upCalls;

	/**
	 * The down calls array indicates whether or not there is a down call on each
	 * floor.
	 */
	private boolean[] downCalls;

	/** The up call pending - true if any up calls exist. */
	private boolean upCallPending;

	/** The down call pending - true if any down calls exit. */
	private boolean downCallPending;

	// TODO: Add any additional fields here..

	/**
	 * Instantiates a new call manager.
	 *
	 * @param floors    the floors
	 * @param numFloors the num floors
	 */
	public CallManager(Floor[] floors, int numFloors) {
		this.floors = floors;
		NUM_FLOORS = numFloors;
		upCalls = new boolean[NUM_FLOORS];
		downCalls = new boolean[NUM_FLOORS];
		upCallPending = false;
		downCallPending = false;

		// TODO: Initialize any added fields here
	}

	/**
	 * Update call status. This is an optional method that could be used to compute
	 * the values of all up and down call fields statically once per tick (to be
	 * more efficient, could only update when there has been a change to the floor
	 * queues - either passengers being added or being removed. The alternative is
	 * to dynamically recalculate the values of specific fields when needed.
	 */
	// Reviewed by Itai Schwarz
	void updateCallStatus() {
		// TODO: Write this method if you choose to implement it...
		upCallPending = false;
		downCallPending = false;
		for (int i = 0; i < floors.length; i++) {
			upCalls[i] = !floors[i].goingUpEmpty();
			if (upCalls[i]) {
				upCallPending = true;
			}
			downCalls[i] = !floors[i].goingDownEmpty();
			if (downCalls[i]) {
				downCallPending = true;
			}
		}
	}

	/**
	 * Prioritize passenger calls from STOP STATE.
	 *
	 * @param lift  the lift
	 * @param floor the floor
	 * @return the passengers
	 */
	// Reviewed by Itai Schwarz
	Passengers prioritizePassengerCalls(Elevator lift) {
		// TODO: Write this method based upon prioritization from STOP...
		// compare numCalls up and down
		Passengers p;
		int floor = lift.getCurrFloor();
		if (!floors[floor].goingUpEmpty() && !floors[floor].goingDownEmpty()) {
			if (numUpCallsPending(floor) >= numDownCallsPending(floor)) {
				p = floors[floor].peekFromUp();
			} else {
				p = floors[floor].peekFromDown();
			}
		} else if (!floors[floor].goingUpEmpty()) {
			p = floors[floor].peekFromUp();
		} else if (!floors[floor].goingDownEmpty()) {
			p = floors[floor].peekFromDown();
		} else {
			if (numUpCallsPending() > numDownCallsPending()) {
				p = getLowestUpCall();
			} else if (numDownCallsPending() > numUpCallsPending()) {
				p = getHighestDownCall();
			} else {
				int distanceToLowestUp = Math.abs(floor - getLowestUpCallFloor());
				int distanceToHighestDown = Math.abs(floor - getHighestDownCallFloor());
				p = distanceToHighestDown < distanceToLowestUp ? getHighestDownCall() : getLowestUpCall();				
			}
		}
		return p;
	}

	// TODO: Write any additional methods here. Things that you might consider:
	// 1. pending calls - are there any? only up? only down?
	// 2. is there a call on the current floor in the current direction
	// 3. How many up calls are pending? how many down calls are pending?
	// 4. How many calls are pending in the direction that the elevator is going
	// 5. Should the elevator change direction?
	//
	// These are an example - you may find you don't need some of these, or you may
	// need more...

	/**
	 * Num up calls pending.
	 *
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numUpCallsPending() {
		int numCalls = 0;
		for (int i = 0; i < upCalls.length; i++) {
			if (upCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Num up calls pending.
	 *
	 * @param floor the floor
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numUpCallsPending(int floor) {
		int numCalls = 0;
		for (int i = floor; i < upCalls.length; i++) {
			if (upCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Num down calls pending.
	 *
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numDownCallsPending() {
		int numCalls = 0;
		for (int i = 0; i < downCalls.length; i++) {
			if (downCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Num down calls pending.
	 *
	 * @param floor the floor
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numDownCallsPending(int floor) {
		int numCalls = 0;
		for (int i = 0; i < floor; i++) {
			if (downCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Num calls pending below floor
	 *
	 * @param floor the floor
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numCallsPendingBelow(int floor) {
		int numCalls = 0;
		for (int i = 0; i < floor; i++) {
			if (downCalls[i] || upCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Num calls pending above floor
	 *
	 * @param floor the floor
	 * @return the int
	 */
	// Reviewed by Itai Schwarz
	int numCallsPendingAbove(int floor) {
		int numCalls = 0;
		for (int i = floor+1; i < floors.length; i++) {
			if (downCalls[i] || upCalls[i]) {
				numCalls++;
			}
		}
		return numCalls;
	}

	/**
	 * Gets the lowest up call.
	 *
	 * @return the lowest up call
	 */
	// Reviewed by Itai Schwarz
	Passengers getLowestUpCall() {
		int floor = 0;
		for (int i = 0; i < upCalls.length; i++) {
			if (upCalls[i]) {
				floor = i;
				break;
			}
		}
		return floors[floor].peekFromUp();
	}

	/**
	 * Gets the highest down call.
	 *
	 * @return the highest down call
	 */
	// Reviewed by Itai Schwarz
	Passengers getHighestDownCall() {
		int saveHighest = 0;
		for (int i = 0; i < downCalls.length; i++) {
			if (downCalls[i]) {
				saveHighest = i;
			}
		}
		return floors[saveHighest].peekFromDown();
	}

	/**
	 * Gets the lowest up call floor.
	 *
	 * @return the lowest up call floor
	 */
	// Reviewed by Itai Schwarz
	int getLowestUpCallFloor() {
		int floor = 0;
		for (int i = 0; i < upCalls.length; i++) {
			if (upCalls[i]) {
				floor = i;
				break;
			}
		}
		return floor;
	}

	/**
	 * Gets the highest down call floor.
	 *
	 * @return the highest down call floor
	 */
	// Reviewed by Itai Schwarz
	int getHighestDownCallFloor() {
		int saveHighest = 0;
		for (int i = 0; i < downCalls.length; i++) {
			if (downCalls[i]) {
				saveHighest = i;
			}
		}
		return saveHighest;
	}

	/**
	 * Checks if is up call pending.
	 *
	 * @return true, if is up call pending
	 */
	// Reviewed by Itai Schwarz
	public boolean isUpCallPending() {
		return upCallPending;
	}

	/**
	 * Checks if is down call pending.
	 *
	 * @return true, if is down call pending
	 */
	// Reviewed by Itai Schwarz
	public boolean isDownCallPending() {
		return downCallPending;
	}

}
