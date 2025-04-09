package lejos.robotics.navigation;

import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

/**
 * <p>Vehicles that are controlled by the SteeringPilot class use a similar steering mechanism to a car, in which the 
 * front wheels pivot from side to side to control direction.</p>
 * 
 * <p>If you issue a command for travel(1000) and then issue a command travel(-500) before
 * it completes the travel(1000) movement, it will call stop, properly inform movement listeners that 
 * the forward movement was halted, and then start moving backward 500 units. This makes movements from the SteeringPilot
 * leak-proof and incorruptible.</p> 
 *
 * <p>Note: A DifferentialPilot robot can simulate a SteeringPilot robot by calling {@link DifferentialPilot#setMinRadius(double)}
 * and setting the value to something greater than zero (example: 15 cm).</p>
 * 
 * @see lejos.robotics.navigation.DifferentialPilot#setMinRadius(double)
 * @author BB
 *
 */
public class SteeringPilot implements ArcMoveController, RegulatedMotorListener {

    private lejos.robotics.RegulatedMotor driveMotor;

    private lejos.robotics.RegulatedMotor steeringMotor;

    private double minTurnRadius;

    private double driveWheelDiameter;

    private boolean isMoving;

    private int oldTacho;

    /**
	 * Rotate motor to this tacho value in order to achieve minimum left hand turn. 
	 */
    private int minLeft;

    /**
	 * Rotate motor to this tacho value in order to achieve minimum right hand turn. 
	 */
    private int minRight;

    /**
	 * Indicates the type of movement (arc, travel) that vehicle is engaged in.
	 */
    private Move moveEvent = null;

    private MoveListener listener = null;

    /**
	 * <p>Creates an instance of the SteeringPilot. The drive wheel measurements are written on the side of the LEGO tire, such
	 * as 56 x 26 (= 56 mm or 5.6 centimeters).</p>
	 * 
	 * The accuracy of this class is dependent on physical factors:
	 * <li> the surface the vehicle is driving on (hard smooth surfaces are much better than carpet)
	 * <li> the accuracy of the steering vehicle (backlash in the steering mechanism will cause turn-angle accuracy problems)
	 * <li> the ability of the steering robot to drive straight (if you see your robot trying to drive straight and it is driving
	 * a curve instead, accuracy will be thrown off significantly) 
	 * <li> When using SteeringPilot with ArcPoseController, the starting position of the robot is also important. Is it truly
	 * lined up with the x axis? Are your destination targets on the floor accurately measured? 
	 * 
	 * <p>Note: If your drive motor is geared for faster movement, you must multiply the wheel size by the 
	 * gear ratio. e.g. If gear ratio is 3:1, multiply wheel diameter by 3.  If your drive motor is geared for
	 * slower movement, divide wheel size by gear ratio.</p> 
	 * 	 * 
	 * @param driveWheelDiameter The diameter of the wheel(s) used to propel the vehicle.
	 * @param driveMotor The motor used to propel the vehicle, such as Motor.B
	 * @param steeringMotor The motor used to steer the steering wheels, such as Motor.C
	 * @param minTurnRadius The smallest turning radius the vehicle can turn. e.g. 41 centimeters
	 * @param leftTurnTacho The tachometer the steering motor must turn to in order to turn left with the minimum turn radius.
	 * @param rightTurnTacho The tachometer the steering motor must turn to in order to turn right with the minimum turn radius.
	 */
    public SteeringPilot(double driveWheelDiameter, lejos.robotics.RegulatedMotor driveMotor, lejos.robotics.RegulatedMotor steeringMotor, double minTurnRadius, int leftTurnTacho, int rightTurnTacho) {
        this.driveMotor = driveMotor;
        this.steeringMotor = steeringMotor;
        this.driveMotor.addListener(this);
        this.driveWheelDiameter = driveWheelDiameter;
        this.minTurnRadius = minTurnRadius;
        this.minLeft = leftTurnTacho;
        this.minRight = rightTurnTacho;
        this.isMoving = false;
    }

    /**
	 * <p>This method calibrates the steering mechanism by turning the wheels all the way to the right and
	 * left until they encounter resistance and recording the tachometer values. These values determine the
	 * outer bounds of steering. The center steering value is the average of the two. NOTE: This method only
	 * works with steering robots that are symmetrical (same maximum steering threshold left and right). </p> 
	 *   
	 *   TODO: Should be able to get steering parity right from this class! No need to fish for boolean.
	 * <p>When you run the method, if you notice the wheels turn left first, then right, it means you need
	 * to set the reverse parameter to true for proper calibration. NOTE: The next time you run the calibrate
	 * method it will still turn left first, but...  </p>
	 * @param reverse Reverses the direction of the steering motor.
	 */
    public void calibrateSteering() {
        steeringMotor.setSpeed(100);
        steeringMotor.setStallThreshold(10, 100);
        steeringMotor.forward();
        while (!steeringMotor.isStalled()) Thread.yield();
        int r = steeringMotor.getTachoCount();
        steeringMotor.backward();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!steeringMotor.isStalled()) Thread.yield();
        int l = steeringMotor.getTachoCount();
        int center = (l + r) / 2;
        r -= center;
        l -= center;
        minRight = r;
        minLeft = l;
        steeringMotor.rotateTo(center);
        steeringMotor.resetTachoCount();
        steeringMotor.setStallThreshold(50, 1000);
    }

    /**
	 * In practice, a robot might steer tighter with one turn than the other.
	 * Currently returns minimum steering radius for the least tight turn direction.  
	 * @return minimum turning radius, in centimeters
	 */
    public double getMinRadius() {
        return minTurnRadius;
    }

    /**
	 * Positive radius = left turn
	 * Negative radius = right turn
	 */
    private double steer(double radius) {
        if (radius == Double.POSITIVE_INFINITY) {
            this.steeringMotor.rotateTo(0);
            return Double.POSITIVE_INFINITY;
        } else if (radius > 0) {
            this.steeringMotor.rotateTo(minLeft);
            return getMinRadius();
        } else {
            this.steeringMotor.rotateTo(minRight);
            return -getMinRadius();
        }
    }

    public void arcForward(double turnRadius) {
        arc(turnRadius, Double.POSITIVE_INFINITY, true);
    }

    public void arcBackward(double turnRadius) {
        arc(turnRadius, Double.NEGATIVE_INFINITY, true);
    }

    public void arc(double turnRadius, double arcAngle) throws IllegalArgumentException {
        if (turnRadius == 0) throw new IllegalArgumentException("SteeringPilot can't do zero radius turns.");
        arc(turnRadius, arcAngle, false);
    }

    public void arc(double turnRadius, double arcAngle, boolean immediateReturn) {
        double distance = Move.convertAngleToDistance((float) arcAngle, (float) turnRadius);
        travelArc(turnRadius, (float) distance, immediateReturn);
    }

    public void setMinRadius(double minTurnRadius) {
        this.minTurnRadius = minTurnRadius;
    }

    public void travelArc(double turnRadius, double distance) {
        travelArc(turnRadius, distance, false);
    }

    public void travelArc(double turnRadius, double distance, boolean immediateReturn) throws IllegalArgumentException {
        double diff = this.getMinRadius() - Math.abs(turnRadius);
        if (diff > 0.1) throw new IllegalArgumentException("Turn radius can't be less than " + this.getMinRadius());
        if (isMoving) stop();
        double actualRadius = steer(turnRadius);
        double angle = Move.convertDistanceToAngle((float) distance, (float) actualRadius);
        moveEvent = new Move((float) distance, (float) angle, true);
        if ((distance == Double.NEGATIVE_INFINITY) | (distance == Double.POSITIVE_INFINITY)) {
            driveMotor.backward();
        }
        int tachos = (int) ((distance * 360) / (driveWheelDiameter * Math.PI));
        driveMotor.rotate(tachos, immediateReturn);
    }

    public void backward() {
        travel(Double.NEGATIVE_INFINITY, true);
    }

    public void forward() {
        travel(Double.POSITIVE_INFINITY, true);
    }

    public double getMaxTravelSpeed() {
        return 0;
    }

    public double getTravelSpeed() {
        return 0;
    }

    public float getMovementIncrement() {
        return 0;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setTravelSpeed(double speed) {
    }

    public void stop() {
        Move oldMove = moveEvent;
        driveMotor.stop();
        while (oldMove == moveEvent) {
            Thread.yield();
        }
    }

    public void travel(double distance) {
        travel(distance, false);
    }

    public void travel(double distance, boolean immediateReturn) {
        travelArc(Double.POSITIVE_INFINITY, distance, immediateReturn);
    }

    public void addMoveListener(MoveListener listener) {
        this.listener = listener;
    }

    public Move getMovement() {
        return null;
    }

    public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stall, long timeStamp) {
        isMoving = true;
        oldTacho = tachoCount;
        if (listener != null) {
            listener.moveStarted(moveEvent, this);
        }
    }

    public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stall, long timeStamp) {
        isMoving = false;
        int tachoTotal = tachoCount - oldTacho;
        float distance = (float) ((tachoTotal / 360f) * Math.PI * driveWheelDiameter);
        float angle = Move.convertDistanceToAngle(distance, moveEvent.getArcRadius());
        moveEvent = new Move(distance, angle, isMoving);
        if (listener != null) {
            listener.moveStopped(moveEvent, this);
        }
    }
}
