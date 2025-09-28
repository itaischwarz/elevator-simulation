
import java.util.ArrayList;

import building.Elevator;
import building.Passengers;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

// TODO: Auto-generated Javadoc
/**
 * The Class ElevatorSimulation.
 */
public class ElevatorSimulation extends Application {

	/** Instantiate the GUI fields. */
	private ElevatorSimController controller;

	/** The num floors. */
	private final int NUM_FLOORS;

	/** The num elevators. */
	private final int NUM_ELEVATORS;

	/** The curr floor. */
	private int currFloor;

	/** The passengers. */
	private int passengers;

	/** The time. */
	private int time;

	/** The t. */
	private Timeline t;

	/** The elevator. */
	private Rectangle elevator;

	/** The bp. */
	private BorderPane bp;

	/** The state label. */
	private Label stateLabel;
	/** The gp. */
	private GridPane gp;

	/** The logging. */
	private Button logging = new Button("Log");

	/** The Step. */
	private Button Step = new Button("Step");

	/** The run. */
	private Button run = new Button("Run");

	/** The enter. */
	private Button enter = new Button("Enter");

	/** The duration. */
	private int duration = 50;

	/** The cycle count. */
	private int cycleCount = 1;

	/** The door 1. */
	private Rectangle door1 = new Rectangle(50, 80);

	/** The door 2. */
	private Rectangle door2 = new Rectangle(50, 80);

	/** The time label. */
	private Label timeLabel = new Label("Time = " + time);

	/** The gp 2. */
	private GridPane gp2;

	/** The Constant MAXCELLY. */
	private final static int MAXCELLY = 13;

	/** The Constant MAXFLOORY. */
	private final static int MAXFLOORY = 13;

	/** The cell Y. */
	private int cellY = 13;

	/** The p label. */
	private Label pLabel;

	/** The sp. */
	private StackPane sp;

	/** The num pass. */
	private int numPass;

	/** The stepticks. */
	private TextField stepticks = new TextField();

	/** The background. */
	private Pane background;

	/** The floor array. */
	private int[] floorArray = { 5, 5, 5, 5, 5, 5 };

	/** The up. */
	private final int UP = 1;

	/** The down. */
	private final int DOWN = -1;

	/** The circ array. */
	private ArrayList<StackPane> circArray = new ArrayList<>();

	/** The pass array. */
	private ArrayList<Passengers> passArray = new ArrayList<>();

	/** Local copies of the states for tracking purposes. */
	private final int STOP = Elevator.STOP;

	/** The mvtoflr. */
	private final int MVTOFLR = Elevator.MVTOFLR;

	/** The opendr. */
	private final int OPENDR = Elevator.OPENDR;

	/** The offld. */
	private final int OFFLD = Elevator.OFFLD;

	/** The board. */
	private final int BOARD = Elevator.BOARD;

	/** The closedr. */
	private final int CLOSEDR = Elevator.CLOSEDR;

	/** The mv1flr. */
	private final int MV1FLR = Elevator.MV1FLR;

	/**
	 * Instantiates a new elevator simulation.
	 */
	// Reviewed by Tohar Markovich
	public ElevatorSimulation() {
		controller = new ElevatorSimController(this);
		NUM_FLOORS = controller.getNumFloors();
		NUM_ELEVATORS = controller.getNumElevators();
		currFloor = controller.getCurrentFloor();

	}

	/**
	 * Initialize the timeline.
	 */
	// Reviewed by Tohar Markovich
	private void initTimeline() {
		t = new Timeline(new KeyFrame(Duration.millis(duration), ae -> controller.stepSim()));

	}

	/**
	 * Start.
	 *
	 * @param primaryStage the primary stage
	 * @throws Exception the exception
	 */
	// Reviewed by Tohar Markovich
	@Override
	public void start(Stage primaryStage) throws Exception {
		gp = new GridPane();
		bp = new BorderPane();
		sp = new StackPane();
		initTimeline();
		makeHotelDoors();
		initializeFloors();
		initializeElevatorPosition();
		makeLegend();
		stepticks.setText("Step n ticks");
		HBox x = new HBox(25);
		pLabel = new Label("" + numPass);
		sp.getChildren().addAll(elevator, pLabel);
		sp.setAlignment(pLabel, Pos.TOP_CENTER);
		makeElevatorDoors();
		x.getChildren().addAll(logging, Step, run, timeLabel, stepticks, enter);
		Step.setOnAction(e -> controller.stepSim());
		logging.setOnAction(e -> enableLogging());
		run.setOnAction(e -> {
			t.setCycleCount(Animation.INDEFINITE);
			t.play();
		});
		enter.setOnAction(e -> {
			setTicks(stepticks.getText());
			t.play();
		});
		setGridPaneConstraints();
		bp.setCenter(gp);
		bp.setTop(x);
		Scene scene = new Scene(bp, 800, 800, Color.BLUE);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Elevator Simulation - " + controller.getTestName());
		primaryStage.show();
	}

	/**
	 * Makes a legend so you can know what passengers are going up or down.
	 */
	// Reviewed by Tohar Markovich
	private void makeLegend() {
		gp.add(new Circle(15, Color.GREEN), 0, 0);
		gp.add(new Circle(15, Color.RED), 0, 1);
		gp.add(new Label(" Up"), 1, 0);
		gp.add(new Label(" Down"), 1, 1);

	}

	/**
	 * Initializes the elevator at the correct floor with the state
	 */
	// Reviewed by Tohar Markovich
	private void initializeElevatorPosition() {
		// TODO Auto-generated method stub
		elevator = new Rectangle(100, 100);
		elevator.setFill(Color.TRANSPARENT);
		elevator.setStroke(Color.LIGHTSTEELBLUE);
		stateLabel = new Label("STOP");
		stateLabel.setWrapText(true);
		for (int i = currFloor; i > 1; i--) {
			cellY -= 2;

		}
		gp.add(stateLabel, 1, cellY);
		gp.add(sp, 2, cellY);
	}

	/**
	 * Makes the background of the building and elevator, makes the doors aswell
	 */
	// Reviewed by Tohar Markovich
	private void makeHotelDoors() {
		Rectangle ElevatorBackground = new Rectangle(102, 625);
		ElevatorBackground.setFill(Color.GRAY);
		gp.add(ElevatorBackground, 2, 8);
		int startingBackP = 3;
		for (int i = 0; i < 6; i++) {
			Rectangle wall1 = new Rectangle(600, 120);
			wall1.setFill(Color.DARKGREY);
			gp.add(wall1, 5, startingBackP);
			startingBackP += 2;
		}
		int startingX = 5;
		int startingY = 0;
		for (int x = 0; x < 18; x++) {
			Rectangle door = new Rectangle(50, 80, Color.SADDLEBROWN);
			Circle doorKnob = new Circle(5, Color.BLACK);
			StackPane doorK = new StackPane(door, doorKnob);
			doorK.setAlignment(doorKnob, Pos.CENTER_LEFT);
			if (x % 6 == 0) {
				startingX += 3;
				startingY = 0;
			}
			gp.add(doorK, startingX, 2 * startingY + 3);
			startingY++;
		}
	}

	/**
	 * Make elevator doors.
	 */
	// Reviewed by Tohar Markovich
	private void makeElevatorDoors() {

		door1.setFill(Color.LIGHTGRAY);
		door2.setFill(Color.LIGHTGRAY);
		door1.setStroke(Color.BLACK);
		door2.setStroke(Color.BLACK);
		sp.getChildren().add(door1);
		sp.getChildren().add(door2);

		sp.setAlignment(door1, Pos.BOTTOM_LEFT);
		sp.setAlignment(door2, Pos.BOTTOM_RIGHT);

	}

	/**
	 * Open door function
	 */
	// Reviewed by Tohar Markovich
	private void Opendr() {
		door1.setWidth(25);
		door2.setWidth(25);
	}

	/**
	 * Close door function
	 */
	// Reviewed by Tohar Markovich
	private void Closedr() {
		door1.setWidth(50);
		door2.setWidth(50);
	}

	/**
	 * Sets the ticks.
	 *
	 * @param ticks the new ticks
	 */
	// Reviewed by Tohar Markovich
	private void setTicks(String ticks) {
		int tick = Integer.parseInt(ticks);
		t.setCycleCount(tick);
		t.play();
	}

	/**
	 * Sets the grid pane constraints.
	 */
	// Reviewed by Tohar Markovich
	private void setGridPaneConstraints() {
		for (int i = 0; i < 16; i++)
			gp.getColumnConstraints().add(new ColumnConstraints(50));

		for (int i = 0; i < 16; i++)
			gp.getRowConstraints().add(new RowConstraints(50));
	}

	/**
	 * Initialize floors of the building accordingly.
	 */
	// Reviewed by Tohar Markovich
	private void initializeFloors() {

		int startingFloor = 14;
		int floor = 1;
		for (int i = 0; i < NUM_FLOORS; i++) {

			Rectangle floorR = new Rectangle(700, 2);
			floorR.setFill(Color.TAN);
			;
			gp.add(floorR, 5, startingFloor);
			Label numFloor = new Label("" + floor);
			numFloor.setFont(Font.font("Cambria", 32));
			gp.add(numFloor, 0, startingFloor - 1);
			startingFloor -= 2;
			floor++;

		}
	}

	/**
	 * Enable logging.
	 */
	// Reviewed by Tohar Markovich
	private void enableLogging() {
		controller.enableLogging();
	}

	/**
	 * Move.
	 *
	 * @param Moves the elevator according to the boolean, if true moves up by one,
	 *              if false moves down.
	 */
	// Reviewed by Tohar Markovich
	private void move(boolean up) {
		if (up) {
			gp.getChildren().remove(sp);
			gp.getChildren().remove(stateLabel);
			cellY -= 2;
			gp.add(sp, 2, cellY);
			gp.add(stateLabel, 1, cellY);
			this.currFloor++;
		} else {
			gp.getChildren().remove(sp);
			gp.getChildren().remove(stateLabel);
			cellY += 2;
			gp.add(sp, 2, cellY);
			gp.add(stateLabel, 1, cellY);
			this.currFloor--;
		}

	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	// Reviewed by Tohar Markovich
	public void setTime(int time) {

		timeLabel.setText("Time = " + time);
	}

	/**
	 * Updates the state, and displays the correct state on the label
	 *
	 * @param current      state
	 * @param currentfloor from elevator
	 */
	// Reviewed by Tohar Markovich
	public void updateState(int currstate, int currFloor, int direction) {
		if (currstate == MV1FLR) {
			if (this.currFloor == currFloor) {
				move(direction == UP);
			}
			stateLabel.setText("MV1FLR");
		} else if (currstate == MVTOFLR) {
			if (this.currFloor == currFloor) {
				move(direction == UP);
			}
			stateLabel.setText("MVTOFR");
		} else if (currstate == OPENDR) {
			Opendr();
			stateLabel.setText("OPENDR");
		} else if (currstate == CLOSEDR) {
			Closedr();
			stateLabel.setText("CLOSDR");
		} else if (currstate == STOP) {
			stateLabel.setText("STOP");
		} else if (currstate == BOARD) {
			stateLabel.setText("BOARD");
		} else if (currstate == OFFLD) {
			stateLabel.setText("OFFLD");
		}
	}

	/**
	 * offloads the number of passengers that are being offloaded
	 *
	 * @param passengers the passengers
	 * @param currFloor  the curr floor
	 */
	// Reviewed by Tohar Markovich
	public void currentNumPassengers(int passengers) {

		this.passengers = passengers;
		int place = 1;

		pLabel.setText("" + this.passengers);

	}

	/**
	 * Boards the correct passenger specified by controller, reflects change onto
	 * gui.
	 *
	 * @param passenger the passenger
	 */
	// Reviewed by Tohar Markovich
	public void board(Passengers[] passenger) {
		int yCord = 0;
		int xCord = 0;
		boolean boarded = false;
		for (int i = 0; i < passenger.length; i++) {
			for (int j = 0; j < passArray.size(); j++) {
				if (passenger[i].equals(passArray.get(j))) {
					StackPane x = circArray.remove(j);
					passArray.remove(j);
					yCord = gp.getColumnIndex(x);
					xCord = gp.getRowIndex(x);
					gp.getChildren().remove(x);
					boarded = true;
					floorArray[currFloor]--;

				}
			}
			if (boarded) { // shifts the other circles over in the floor
				for (int n = 0; n < circArray.size(); n++) {
					StackPane y = circArray.get(n);
					int sCordy = gp.getRowIndex(y);
					int sCordX = gp.getColumnIndex(y);
					if (sCordy == xCord && sCordX > yCord) {
						gp.getChildren().remove(y);
						gp.add(y, --sCordX, sCordy);

					}
				}
			}
			boarded = false;
		}
	}

	/**
	 * if any passengers give up, the controller passes it in and it is reflected by
	 * the gui.Similar logic to the board
	 *
	 * @param passenger the passenger
	 */
	// Reviewed by Tohar Markovich
	public void giveUp(Passengers[] passenger) {
		int yCord = 0;
		int xCord = 0;
		boolean boarded = false;
		for (int i = 0; i < passenger.length; i++) {
			for (int j = 0; j < passArray.size(); j++) {
				if (passenger[i].equals(passArray.get(j))) {
					StackPane x = circArray.remove(j);
					passArray.remove(j);
					yCord = gp.getColumnIndex(x);
					xCord = gp.getRowIndex(x);
					gp.getChildren().remove(x);
					boarded = true;
					floorArray[currFloor]--;

				}
			}
			if (boarded) { // shifts the other circles over in the floor
				for (int n = 0; n < circArray.size(); n++) {
					StackPane y = circArray.get(n);
					int sCordy = gp.getRowIndex(y);
					int sCordX = gp.getColumnIndex(y);
					if (sCordy == xCord && sCordX > yCord) {
						gp.getChildren().remove(y);
						gp.add(y, --sCordX, sCordy);

					}
				}
			}
			boarded = false;
		}
	}

	/**
	 * Called by the controller, passes in an array of passengers that have arrived,
	 * reflects change on gui.
	 *
	 * @param passengers the passengers
	 */
	// Reviewed by Tohar Markovich
	public void arrivalPassengers(Passengers[] passengers) {
		for (int i = 0; i < passengers.length; i++) {
			int floor = MAXFLOORY - ((passengers[i].getOnFloor()) * 2);
			if (passengers[i].getDirection() == UP) {
				Label numP = new Label("" + passengers[i].getNumPass());
				Circle x = new Circle(25);
				StackPane y = new StackPane(x, numP);
				x.setFill(Color.GREEN);

				gp.add(y, floorArray[passengers[i].getOnFloor()], floor);
				circArray.add(y);
				passArray.add(passengers[i]);
				floorArray[passengers[i].getOnFloor()]++;
			} else {
				Label numP = new Label("" + passengers[i].getNumPass());
				Circle x = new Circle(25);
				StackPane y = new StackPane(x, numP);
				x.setFill(Color.RED);
				gp.add(y, floorArray[passengers[i].getOnFloor()], floor);
				circArray.add(y);
				passArray.add(passengers[i]);
				floorArray[passengers[i].getOnFloor()]++;
			}
		}

	}

	/**
	 * End sim.
	 */
	// Reviewed by Tohar Markovich
	public void endSim() {
		t.stop();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

}
