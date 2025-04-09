package edu.mapi.ir.state;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author ZP
 */
public class EstimatedState {

    private double x;

    private double y;

    private double rotation;

    private double lOutPow;

    private double rOutPow;

    public boolean groundSensor;

    public boolean bumperSensor;

    public double externalCompassWeight = 0.00;

    protected NumberFormat df = DecimalFormat.getInstance(Locale.US);

    public double[] distanceSensors = new double[4];

    /**
	 * Default constructor
	 */
    public EstimatedState() {
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(3);
        for (int i = 0; i < 4; i++) distanceSensors[i] = -1;
    }

    public void setExternalCompassValue(double angleDegs) {
        double angle = angleDegs;
        angle = Math.toRadians(angle);
        if (Math.abs(angle - rotation) > 0.5) return;
        this.rotation = externalCompassWeight * angle + (1.0 - externalCompassWeight) * rotation;
    }

    /**
	 * Apply the given actuation to the current estimated state (dead reckoning)
	 * @param leftPower Left motor actuation
	 * @param rightPower Right motor actuation
	 */
    public void actuate(double leftPower, double rightPower) {
        lOutPow = (lOutPow + leftPower) / 2;
        rOutPow = (rOutPow + rightPower) / 2;
        double distWalked = (lOutPow + rOutPow) / 2;
        rotation += (rOutPow - lOutPow);
        while (rotation > Math.PI) rotation -= Math.PI * 2;
        while (rotation < -Math.PI) rotation += Math.PI * 2;
        x += Math.cos(rotation) * distWalked;
        y += Math.sin(rotation) * distWalked;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
	 * @return The current estimated rotation, in radians
	 */
    public double getRotation() {
        return rotation;
    }

    /**
	 * Change the currently estimated rotation
	 * @param rotation The rotation in radians
	 */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "(" + df.format(x) + "," + df.format(y) + "," + df.format(Math.toDegrees(rotation)) + ")";
    }

    /**
	 * The effective power that was given to the left motor
	 * @return The last power given to the left motor (see CiberMouse documentation)
	 */
    public double getLOutPow() {
        return lOutPow;
    }

    /**
	 * The effective power that was given to the right motor
	 * @return The last power given to the right motor (see CiberMouse documentation)
	 */
    public double getROutPow() {
        return rOutPow;
    }
}
