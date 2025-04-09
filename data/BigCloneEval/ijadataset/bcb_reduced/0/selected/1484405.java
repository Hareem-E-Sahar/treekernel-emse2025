package com.googlecode.grtframework.actuator;

import com.googlecode.grtframework.rpc.RPCConnection;
import com.googlecode.grtframework.rpc.RPCMessage;

/**
 * A servo that is actuated on the other side of an RPC connection.
 * 
 * @author ajc
 * 
 */
public class RPCServo implements IServo {

    private final RPCConnection out;

    private final int key;

    private final double pwmRange;

    private final double average;

    private final double rotationRange;

    /**
	 * Constructs a new calibrated Servo given 2 calibration points.
	 * 
	 * @param out
	 * @param key
	 * @param leftPWM
	 * @param leftAngle
	 * @param rightPWM
	 * @param rightAngle
	 */
    public RPCServo(RPCConnection out, int key, int leftPWM, double leftAngle, int rightPWM, double rightAngle) {
        this.out = out;
        this.key = key;
        pwmRange = rightPWM - leftPWM;
        average = (rightPWM + leftPWM) / 2;
        rotationRange = rightAngle - leftAngle;
    }

    @Override
    public void setPercentPosition(double percent) {
        setPWM((int) (average - (percent * pwmRange / 2)));
    }

    @Override
    public void setPosition(double radians) {
        setPercentPosition(2 * radians / rotationRange);
    }

    /**
	 * Sends a PWM value to the servo
	 * 
	 * @param value
	 */
    protected void setPWM(int value) {
        System.out.println(value);
        if (out != null) {
            out.send(new RPCMessage(key, value));
        }
    }
}
