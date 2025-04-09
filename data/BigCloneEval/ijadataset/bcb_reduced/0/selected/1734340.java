package lejos.robotics.navigation;

import java.util.ArrayList;
import lejos.nxt.addon.GyroSensor;
import lejos.robotics.EncoderMotor;

/**
 * <p>Allow standard moves with a Segway robot. Currently the robot has a 5 second delay between moves to allow
 * it some time to rebalance and straighten out uneven tacho rotations. This can be changed with setMoveDelay().</p>
 *  
 * <p>This code will work with any Segway robot, but tall Segway robots will have problems balancing when the robot
 * is moving. To counteract this, use larger wheels and/or slow down the speed using setTravelSpeed(). Make sure the 
 * battery is <b>fully charged</b>. The robot is more stable on carpet than hardwood at higher speeds.</p> 
 *  
 * <p>The default speed is 50, which can be changed with setTravelSpeed().</p>  
 * 
 * @see lejos.robotics.navigation.Segway
 * @author BB
 *
 */
public class SegwayPilot extends Segway implements ArcRotateMoveController {

    private int move_delay = 5000;

    private double wheelDiameter;

    private double trackWidth;

    private long left_start_tacho;

    private long right_start_tacho;

    /**
	 * MoveListeners to notify when a move is started or stopped.
	 */
    private ArrayList<MoveListener> listeners = new ArrayList<MoveListener>();

    /**
	 * Creates an instance of SegwayPilot.
	 * 
	 * @param left The left motor. 
	 * @param right The right motor. 
	 * @param gyro A generic gyroscope
	 * @param wheelDiameter The diameter of the wheel. For convenience, use the WHEEL_SIZE_XXX constants.
	 * @param trackWidth Distance between the center of the right tire and left tire. Use the same units as wheelDiameter.
	 */
    public SegwayPilot(EncoderMotor left, EncoderMotor right, GyroSensor gyro, double wheelDiameter, double trackWidth) {
        super(left, right, gyro, wheelDiameter);
        this.left_tacho_target = left.getTachoCount();
        this.right_tacho_target = right.getTachoCount();
        this.trackWidth = trackWidth;
        this.wheelDiameter = wheelDiameter;
        new MoveControlRegulator().start();
        try {
            Thread.sleep(move_delay);
        } catch (InterruptedException e) {
        }
    }

    /**
	 * Calculates the turn rate corresponding to the turn radius; <br>
	 * use as the parameter for steer() negative argument means center of turn
	 * is on right, so angle of turn is negative
	 * @param radius
	 * @return turnRate to be used in steer()
	 */
    private float turnRate(final float radius) {
        int direction;
        float radiusToUse;
        if (radius < 0) {
            direction = -1;
            radiusToUse = -radius;
        } else {
            direction = 1;
            radiusToUse = radius;
        }
        double ratio = (2 * radiusToUse - trackWidth) / (2 * radiusToUse + trackWidth);
        return (float) (direction * 100 * (1 - ratio));
    }

    private void steerPrep(double turnRate, double angle) {
        left_start_tacho = left_tacho_target;
        right_start_tacho = right_tacho_target;
        float steerRatio;
        double rate = turnRate;
        if (rate < -200) rate = -200;
        if (rate > 200) rate = 200;
        int side = (int) Math.signum(turnRate);
        steerRatio = (float) (1 - rate / 100.0);
        int rotAngle = (int) (angle * trackWidth * 2 / (wheelDiameter * (1 - steerRatio)));
        if (turnRate < 0) {
            left_tacho_target -= (side * rotAngle);
            right_tacho_target -= (int) (side * rotAngle * steerRatio);
        } else {
            left_tacho_target -= (int) (side * rotAngle * steerRatio);
            right_tacho_target -= (side * rotAngle);
        }
        if (angle > 0) {
            move_mode = ARC_F;
            wheelDriver(-Math.round(SPEED * steerRatio), -SPEED);
        } else {
            move_mode = ARC_B;
            wheelDriver(Math.round(SPEED * steerRatio), SPEED);
        }
    }

    /**
	 * Moves the robot along a curved path for a specified angle of rotation. This method is similar to the
	 * {@link #arc(double radius, double angle, boolean immediateReturn)} method except it uses the <code> turnRate()</code>
	 * parameter to determine the curvature of the path and therefore has the ability to drive straight.
	 * This makes it useful for line following applications. This method has the ability to return immediately
	 * by using the <code>immediateReturn</code> parameter set to <b>true</b>.
	 *
	 * <p>
	 * The <code>turnRate</code> specifies the sharpness of the turn. Use values between -200 and +200.<br>
	 * For details about how this parameter works, see {@link lejos.robotics.navigation.DifferentialPilot#steer(double, double)}
	 * <p>
	 * The robot will stop when its heading has changed by the amount of the  <code>angle</code> parameter.<br>
	 * If <code>angle</code> is positive, the robot will move in the direction that increases its heading (it turns left).<br>
	 * If <code>angle</code> is negative, the robot will move in the direction that decreases its heading (turns right).<br>
	 * If <code>angle</code> is zero, the robot will not move and the method returns immediately.<br>
	 * For more details about this parameter, see {@link lejos.robotics.navigation.DifferentialPilot#steer(double, double)}
	 * <p>
	 * Note: If you have specified a drift correction in the constructor it will not be applied in this method.
	 *
	 * @param turnRate If positive, the left side of the robot is on the inside of the turn. If negative,
	 * the left side is on the outside.
	 * @param angle The angle through which the robot will rotate. If negative, robot traces the turning circle backwards.
	 * @param immediateReturn If immediateReturn is true then the method returns immediately.
	 */
    public void steer(double turnRate, double angle, boolean immediateReturn) {
        this.arc_target_angle = (float) angle;
        if (angle == 0) return;
        if (turnRate == 0) {
            forward();
            return;
        }
        steerPrep((float) turnRate, (float) angle);
        if (!immediateReturn) while (move_mode != STOP) ;
        try {
            Thread.sleep(move_delay);
        } catch (InterruptedException e) {
        }
    }

    private int angle_parity = 1;

    public void arc(double radius, double angle, boolean immediateReturn) {
        for (MoveListener ml : listeners) ml.moveStarted(new Move(true, (float) angle, (float) radius), this);
        if (radius == Double.POSITIVE_INFINITY || radius == Double.NEGATIVE_INFINITY) {
            forward();
            return;
        }
        if (radius < 0) angle_parity = -1; else angle_parity = 1;
        steer(turnRate((float) radius), angle, immediateReturn);
    }

    public void forward() {
        travel(Double.POSITIVE_INFINITY, true);
    }

    public void backward() {
        travel(Double.NEGATIVE_INFINITY, true);
    }

    /**
	 * Set the delay between movements which allows the Segway to recover balance. Default value is 
	 * five seconds (5000 millis).
	 */
    public void setMoveDelay(int millis) {
        move_delay = millis;
    }

    private long arc_target_tacho_avg;

    private double arc_target_angle;

    public void stop() {
        arc_target_tacho_avg = left_start_tacho - left_tacho_target;
        arc_target_tacho_avg += right_start_tacho - right_tacho_target;
        arc_target_tacho_avg /= 2;
        this.left_tacho_target = left_motor.getTachoCount();
        this.right_tacho_target = right_motor.getTachoCount();
        int previous_move_mode = move_mode;
        move_mode = STOP;
        wheelDriver(0, 0);
        if (previous_move_mode == ROTATE_L || previous_move_mode == ROTATE_R) calcRotateNotify();
        if (previous_move_mode == ARC_F || previous_move_mode == ARC_B) calcArcNotify(); else calcTravelNotify();
    }

    /**
	 * This method calculates the total distance traveled in straight lines.
	 */
    private void calcTravelNotify() {
        long tacho_total = this.left_start_tacho - this.left_tacho_target;
        tacho_total += this.right_start_tacho - this.right_tacho_target;
        tacho_total /= 2;
        double circ = Math.PI * wheelDiameter;
        double dist = (circ * tacho_total) / 360;
        for (MoveListener ml : listeners) ml.moveStopped(new Move((float) dist, 0, false), this);
    }

    /**
	 * This method calculates the distance and angle change of an arc.
	 */
    private void calcArcNotify() {
        long left_tacho_total = this.left_start_tacho - this.left_tacho_target;
        long right_tacho_total = this.right_start_tacho - this.right_tacho_target;
        long tacho_total = (left_tacho_total + right_tacho_total) / 2;
        double circ = Math.PI * wheelDiameter;
        double dist = (circ * tacho_total) / 360;
        double angle = (1.0 * tacho_total) / arc_target_tacho_avg;
        angle *= arc_target_angle;
        angle *= angle_parity;
        for (MoveListener ml : listeners) ml.moveStopped(new Move((float) dist, (float) angle, false), this);
    }

    /**
	 * Calculates the total angle made after a rotation completes.
	 */
    private void calcRotateNotify() {
        long tacho_total = this.left_tacho_target - this.left_start_tacho;
        tacho_total += this.right_start_tacho - this.right_tacho_target;
        tacho_total /= 2;
        double dist = (tacho_total * wheelDiameter * Math.PI) / 360;
        double angle = (360 * dist) / (trackWidth * Math.PI);
        for (MoveListener ml : listeners) ml.moveStopped(new Move(0, (float) angle, false), this);
    }

    public void travel(double distance, boolean immediateReturn) {
        left_start_tacho = left_tacho_target;
        right_start_tacho = right_tacho_target;
        double circ = Math.PI * wheelDiameter;
        long degree_rotations = (long) ((distance / circ) * 360);
        this.left_tacho_target -= degree_rotations;
        this.right_tacho_target -= degree_rotations;
        if (distance == Double.POSITIVE_INFINITY) {
            this.left_tacho_target = Integer.MIN_VALUE;
            this.right_tacho_target = Integer.MIN_VALUE;
        } else if (distance == Double.NEGATIVE_INFINITY) {
            this.left_tacho_target = Integer.MAX_VALUE;
            this.right_tacho_target = Integer.MAX_VALUE;
        }
        if (distance > 0) {
            move_mode = FORWARD_T;
            wheelDriver(-SPEED, -SPEED);
        } else {
            move_mode = BACKWARD_T;
            wheelDriver(SPEED, SPEED);
        }
        for (MoveListener ml : listeners) ml.moveStarted(new Move(Move.MoveType.TRAVEL, (float) distance, 0, true), this);
        if (!immediateReturn) while (move_mode != STOP) ;
        try {
            Thread.sleep(move_delay);
        } catch (InterruptedException e) {
        }
    }

    public void rotate(double degrees, boolean immediateReturn) {
        double circleCirc = trackWidth * Math.PI;
        double circleDist = (degrees / 360) * circleCirc;
        double wheelCirc = this.wheelDiameter * Math.PI;
        long degree_rotations = Math.round((circleDist / wheelCirc) * 360);
        left_start_tacho = left_tacho_target;
        right_start_tacho = right_tacho_target;
        left_tacho_target += degree_rotations;
        right_tacho_target -= degree_rotations;
        if (degrees > 0) {
            move_mode = ROTATE_L;
            wheelDriver(SPEED, -SPEED);
        } else {
            move_mode = ROTATE_R;
            wheelDriver(-SPEED, SPEED);
        }
        for (MoveListener ml : listeners) ml.moveStarted(new Move(Move.MoveType.ROTATE, 0, (float) degrees, true), this);
        if (!immediateReturn) while (move_mode != STOP) ;
        try {
            Thread.sleep(move_delay);
        } catch (InterruptedException e) {
        }
    }

    public double getMaxTravelSpeed() {
        return 200;
    }

    public double getMovementIncrement() {
        return 0;
    }

    public double getTravelSpeed() {
        return SPEED;
    }

    /**
	 * Currently this method isn't properly implemented with the proper units. Speed is just
	 * an arbitrary number up to about 200. At higher speed values it can be unstable. Currently it
	 * uses a default of 80 which is near the unstable speed.
	 * 
	 * Will need to make this method use units/second. 
	 * @param speed The speed to travel.
	 */
    public void setTravelSpeed(double speed) {
        SPEED = (int) speed;
    }

    public boolean isMoving() {
        if (move_mode == STOP) return false; else return true;
    }

    public void travel(double distance) {
        travel(distance, false);
    }

    public void addMoveListener(MoveListener m) {
        listeners.add(m);
    }

    public Move getMovement() {
        return null;
    }

    public float getAngleIncrement() {
        return 0;
    }

    public double getRotateMaxSpeed() {
        return 0;
    }

    public double getRotateSpeed() {
        return 0;
    }

    public void rotate(double angle) {
        rotate(angle, false);
    }

    public void setRotateSpeed(double arg0) {
    }

    public void arc(double radius, double angle) {
        arc(radius, angle, false);
    }

    public void arcBackward(double radius) {
        arc(radius, Double.NEGATIVE_INFINITY, true);
    }

    public void arcForward(double radius) {
        arc(radius, Double.POSITIVE_INFINITY, true);
    }

    private double minRadius = 0;

    public double getMinRadius() {
        return minRadius;
    }

    public void setMinRadius(double radius) {
        this.minRadius = radius;
    }

    public void travelArc(double radius, double distance) {
        travelArc(radius, distance, false);
    }

    public void travelArc(double radius, double distance, boolean immediateReturn) {
        if (radius == Double.POSITIVE_INFINITY || radius == Double.NEGATIVE_INFINITY) {
            travel(distance, immediateReturn);
            return;
        }
        if (radius == 0) {
            throw new IllegalArgumentException("Zero arc radius");
        }
        float angle = (float) ((distance * 180) / (Math.PI * radius));
        arc(radius, angle, immediateReturn);
    }

    private long left_tacho_target;

    private long right_tacho_target;

    public int SPEED = 50;

    public static final int STOP = 0;

    public static final int FORWARD_T = 1;

    public static final int BACKWARD_T = 2;

    public static final int ROTATE_L = 3;

    public static final int ROTATE_R = 4;

    public static final int ARC_F = 5;

    public static final int ARC_B = 6;

    private int move_mode = STOP;

    /**
	 * This thread runs in parallel to the balance thread.  This thread monitors
	 * the current move mode (STOP, FORWARD, etc...) and seeks to carry out the move.
	 */
    private class MoveControlRegulator extends Thread {

        /**
		 * Period of time (in ms) between checking the control situation
		 */
        private static final int MONITOR_INTERVAL = 7;

        /**
		 * Maximum impulse (kind of like speed) when correcting position in stop() mode.
		 */
        private static final int MAX_CORRECTION = 5;

        public MoveControlRegulator() {
            this.setDaemon(true);
        }

        public void run() {
            while (true) {
                switch(move_mode) {
                    case STOP:
                        long leftDiff = (left_tacho_target - left_motor.getTachoCount()) / 2;
                        long rightDiff = (right_tacho_target - right_motor.getTachoCount()) / 2;
                        if (leftDiff > MAX_CORRECTION) leftDiff = MAX_CORRECTION;
                        if (rightDiff > MAX_CORRECTION) rightDiff = MAX_CORRECTION;
                        wheelDriver((int) leftDiff, (int) rightDiff);
                        break;
                    case ARC_F:
                    case FORWARD_T:
                        long left_diff = left_motor.getTachoCount() - left_tacho_target;
                        long right_diff = right_motor.getTachoCount() - right_tacho_target;
                        if (left_diff < 0 && right_diff < 0) {
                            arc_target_tacho_avg = left_start_tacho - left_tacho_target;
                            arc_target_tacho_avg += right_start_tacho - right_tacho_target;
                            arc_target_tacho_avg /= 2;
                            int prev_move_mode = move_mode;
                            wheelDriver(0, 0);
                            move_mode = STOP;
                            if (prev_move_mode == FORWARD_T) calcTravelNotify(); else if (prev_move_mode == ARC_F) calcArcNotify();
                        }
                        break;
                    case ARC_B:
                    case BACKWARD_T:
                        left_diff = left_motor.getTachoCount() - left_tacho_target;
                        right_diff = right_motor.getTachoCount() - right_tacho_target;
                        if (left_diff > 0 && right_diff > 0) {
                            arc_target_tacho_avg = left_start_tacho - left_tacho_target;
                            arc_target_tacho_avg += right_start_tacho - right_tacho_target;
                            arc_target_tacho_avg /= 2;
                            int prev_move_mode = move_mode;
                            wheelDriver(0, 0);
                            move_mode = STOP;
                            if (prev_move_mode == BACKWARD_T) calcTravelNotify(); else if (prev_move_mode == ARC_B) calcArcNotify();
                        }
                        break;
                    case ROTATE_L:
                        left_diff = left_motor.getTachoCount() - left_tacho_target;
                        right_diff = right_motor.getTachoCount() - right_tacho_target;
                        if (left_diff > 0 && right_diff < 0) {
                            wheelDriver(0, 0);
                            move_mode = STOP;
                            Thread.yield();
                            calcRotateNotify();
                        }
                        break;
                    case ROTATE_R:
                        left_diff = left_motor.getTachoCount() - left_tacho_target;
                        right_diff = right_motor.getTachoCount() - right_tacho_target;
                        if (left_diff < 0 && right_diff > 0) {
                            wheelDriver(0, 0);
                            move_mode = STOP;
                            Thread.yield();
                            calcRotateNotify();
                        }
                        break;
                }
                try {
                    Thread.sleep(MONITOR_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
