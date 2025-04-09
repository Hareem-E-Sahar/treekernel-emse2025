import lejos.nxt.ADSensorPort;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;

/**
 * This class extends <code>lejos.nxt.GyroSensor</code> and implements a rudimentary integrator to provide fairly accurate 
 * inertial guidance
 * capability using the Hitechnic Gyro sensor.
 * <P>
 * When instantiated, a thread is spawned to constantly monitor the gyro sensor readings over time to calculate the rate of 
 * change and thus calculate and provide a relative angular vector through the <code>readHeading()</code> method.
 * <h3>Assumptions:</h3>
 * <ul>
 * <li>The Hitechnic Gyro sensor NGY1044 is being used. (<a href="http://www.hitechnic.com/" target=-"_blank">
 * http://www.hitechnic.com/</a>)
 * <li>Release 0.5 of LeJOS NXJ is being used. This class has been tested with that version and the assumption is made
 * that forward releases will remain compatible.
 * </ul>
 * @author Kirk P. Thompson
 * @version v0.2  2/5/08
 */
public class GyroSensor extends lejos.nxt.addon.GyroSensor {

    private final int SAMPLESPAN = 10;

    private final int SENSOR_CONSTANT = 1000;

    private final int DIRTYACCUMLATORVAL = -999999;

    private Integrator integrator = new Integrator();

    private volatile int startTime, endTime, timeDelta;

    private volatile int sensorValue, sensorValueRaw;

    private volatile int offset = 0, zeroRangeMin = 0, zeroRangeMax = 0;

    private volatile int minVal, maxVal, maxTimeDeltaVal;

    private volatile int integralVectorAccumulator = DIRTYACCUMLATORVAL;

    /**
     * Creates and initializes a  new <code>GyroSensor</code> listening on the passed <code>SensorPort</code>. The associated 
     * integrator thread runs 
     * as a daemon. 
     * 
     * @param port The <code>SensorPort</code> the Gyro is connected to
     */
    public GyroSensor(SensorPort port) {
        super((ADSensorPort) port);
        super.setOffset(0);
        resetExtents();
        integrator.start();
    }

    /**
     * Override the parent classes' <code>setOffset()</code> method to do nothing. This is
     * because we manage the offset in this class with <code>setZero()</code> and don't want any "interference".
     * 
     * @param offset The offset value that will be ignored :-P
     * @see #setZero()
     */
    public void setOffset(int offset) {
    }

    private class Integrator extends Thread {

        public Integrator() {
            this.setPriority(NORM_PRIORITY + 1);
            this.setDaemon(true);
        }

        public void run() {
            for (; ; ) {
                sensorValueRaw = getSensorAverage(SAMPLESPAN);
                sensorValue = normalizeReading(sensorValueRaw);
                timeDelta = endTime - startTime;
                if (timeDelta > maxTimeDeltaVal) maxTimeDeltaVal = timeDelta;
                if (offset != 0) integralVectorAccumulator += sensorValue * timeDelta;
            }
        }
    }

    private void doWait(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The AVR samples at 333Hz (~3 samples/ms). We pull ~1 sample/ms here.
     * 
     * @param sampleTimeRange span of sample time in milliseconds
     * @return The average of sample values taken during span
     */
    private int getSensorAverage(int sampleTimeRange) {
        int accumBucket, samplecount, reading;
        boolean first = true;
        samplecount = 1;
        accumBucket = 0;
        for (; ; ) {
            if (first) {
                startTime = (int) System.currentTimeMillis();
                accumBucket = getRawReading();
                first = false;
                doWait(1);
            }
            reading = getRawReading();
            endTime = (int) System.currentTimeMillis();
            accumBucket += reading;
            samplecount++;
            if (endTime - startTime >= sampleTimeRange) break;
            doWait(1);
        }
        reading = accumBucket / samplecount;
        return reading;
    }

    /**
     * Calculate and set the zero-window to offset(normalize) the sensor reading. Make sure the Gyro sensor is static 
     * (not moving) when calling
     * this method. This method takes approximately 2-6 seconds.
     * <P>
     * If this method is not called after instantiation, the <code>readHeading()</code> value returned will always return 0. If 
     * the sensor is moving, the last valid <code>setZero()</code> results are re-used.
     * 
     * @see #getNParms()
     * @see #readHeading()
     * @see #resetExtents()
     * @see #setZeroHeading()
     */
    public void setZero() {
        final int TIMEOUT = 1000;
        final int DESIRED_DELTA = 3;
        final int INIT_MIN = 999;
        final int INIT_MAX = -999;
        final int MAX_TRIES = 6;
        int startTime;
        int reading;
        int minRead = INIT_MIN, maxRead = INIT_MAX;
        doWait(TIMEOUT);
        for (int i = 0; i < MAX_TRIES; i++) {
            startTime = (int) System.currentTimeMillis();
            for (; ; ) {
                if ((int) System.currentTimeMillis() - startTime > TIMEOUT) break;
                reading = getRawReading();
                doWait(1);
                if (reading < minRead) minRead = reading;
                if (reading > maxRead) maxRead = reading;
            }
            if (maxRead - minRead == DESIRED_DELTA) break;
            if (maxRead - minRead <= DESIRED_DELTA && i > MAX_TRIES / 2) break;
            minRead = INIT_MIN;
            maxRead = INIT_MAX;
        }
        if (minRead == INIT_MIN) return;
        zeroRangeMin = minRead;
        zeroRangeMax = maxRead;
        setZeroHeading();
        offset = (minRead + maxRead) / 2;
    }

    /** 
     * Return the parameters calculated by <code>setZero()</code> in an <code>int[]</code> array. The <code>setZero()</code>
     * method samples raw readings from the sensor and determines the min and max values to use as the "zero-window". Any readings 
     * that fall into this range are zeroed so the integrator doesn't drift when the Gyro sensor is stationary. 
     * <p> The bias offset is the average between the min and max and is used to offset the raw reading values to provide
     * a positive/negative vector.
     *
     * @return An array of <code>int[]</code> as follows: <ul><li>Element[0]: The offset value. <li>Element[1]: The min 
     * zero-window value. <li>Element[2]: The max 
     * zero-window value.</ul>
     * @see #setZero()
     */
    public int[] getNParms() {
        int[] retVal = new int[3];
        retVal[0] = offset;
        retVal[1] = zeroRangeMin;
        retVal[2] = zeroRangeMax;
        return retVal;
    }

    private int getRawReading() {
        return super.readValue();
    }

    /**
     * Return the last normalized gyro sensor value. The normalization bias is set by <code>setZero()</code>.
     * 
     * @return The normalized gyro value. Raw value if <code>setZero()</code> has not been called.
     * @see #setZero()
     * @see #readRawValue()
     */
    public int readValue() {
        return sensorValue;
    }

    /**
     * Return the raw gyro sensor value. 
     * 
     * @return The last read raw gyro value. 
     * @see #readValue()
     */
    public int readRawValue() {
        return sensorValueRaw;
    }

    /**
     * Return the heading value in degrees. Before this returns a valid value, you must call <code>setZero()</code> to 
     * calculate the sensor bias and zero-range. The heading value will be negative for counter-clockwise and positive
     * for clock-wise. 0 (zero) degrees is the direction of Gyro sensor pointed straight forward after a <code>setZero()</code>.
     * <p>
     * <b>Caveat Emptor:</b> Due to the inexact integration algorithm, timing granularity, and [potential] sensor error, this
     * value is "close" but not perfect. Remember, "close" only counts in Horseshoes and hand-grenades...
     * 
     * @return The calculated heading value.
     * @see #readValue()
     * @see #setZero()
     */
    public float readHeading() {
        float retVal = (float) integralVectorAccumulator / SENSOR_CONSTANT;
        if (integralVectorAccumulator == DIRTYACCUMLATORVAL) retVal = 0f;
        return retVal;
    }

    /**
     * Get MIN normalized sensor reading. Raw if <code>setZero()</code> has not been called.
     * @return The minimum since the last <code>resetExtents()</code>
     * @see #resetExtents()
     */
    public int getMin() {
        return minVal;
    }

    /**
     * Get MAX normalized sensor reading. Raw if <code>setZero()</code> has not been called.
     * @return The maximum since the last <code>resetExtents()</code>
     * @see #resetExtents()
     */
    public int getMax() {
        return maxVal;
    }

    private int normalizeReading(int sensorValueRaw) {
        int sensorValue = 0;
        if (sensorValueRaw >= zeroRangeMin && sensorValueRaw <= zeroRangeMax) {
            sensorValue = 0;
        } else {
            sensorValue = sensorValueRaw - offset;
        }
        if (sensorValue < minVal) minVal = sensorValue;
        if (sensorValue > maxVal) maxVal = sensorValue;
        return sensorValue;
    }

    /**
     * Reset the MIN and MAX values:
     *
     * @see #setZero()
     * @see #display()
     * @see #getMin()
     * @see #getMax()
     */
    public void resetExtents() {
        minVal = 9999;
        maxVal = -9999;
        maxTimeDeltaVal = -99;
    }

    /**
     * Reset the current heading to zero. This doesn't re-calculate the zero-window, just the heading accumulator.
     * 
     *  @see #setZero()
     */
    public void setZeroHeading() {
        synchronized (integrator) {
            integralVectorAccumulator = 0;
        }
    }

    /**
     * Display statistics of this class as it executes. LEFT button does a <code>setZero()</code>, 
     * ENTER button does a <code> setZeroHeading()</code>, 
     * RIGHT button does a 
     * <code>resetExtents()</code>, ESCAPE button exits the display mode and hence, this method.
     * <P> Values displayed:
     * <pre>
     * nrml:   The normalized sensor reading. Raw if <code>setZero()</code>. (see {@link #readValue()})
     * &nbsp;       has not been called.
     * bias:   The bias (offset) value added to raw reading to derive nrml.
     * zspan:  The zero-span values calculated.
     * raw:    The raw sensor value read. (see {@link #readRawValue()})
     * tdelta: The sampling time span that the last sensor reading occured over.
     * hdng:   The heading in degrees.
     * </pre>
     * @see #setZero()
     * @see #resetExtents()
     * @see #getNParms()
     */
    public void display() {
        final int COL1 = 8, COL2 = 12;
        LCD.clearDisplay();
        LCD.drawString("         MIN MAX", 0, 0);
        LCD.drawString("zspan:", 0, 3);
        LCD.drawInt(zeroRangeMin, 4, COL1, 3);
        LCD.drawInt(zeroRangeMax, 4, COL2, 3);
        LCD.drawString("bias:" + offset + "   ", 0, 2);
        while (!Button.ESCAPE.isPressed()) {
            if (Button.RIGHT.isPressed()) resetExtents();
            if (Button.ENTER.isPressed()) setZeroHeading();
            if (Button.LEFT.isPressed()) {
                LCD.drawString("zspan:", 0, 3);
                LCD.drawString("---- ----", COL1, 3);
                setZero();
                LCD.drawInt(zeroRangeMin, 4, COL1, 3);
                LCD.drawInt(zeroRangeMax, 4, COL2, 3);
                LCD.drawString("bias:" + offset + "   ", 0, 2);
            }
            LCD.drawString("nrml:" + readValue() + "   ", 0, 1);
            LCD.drawInt(minVal, 4, COL1, 1);
            LCD.drawInt(maxVal, 4, COL2, 1);
            LCD.drawString("raw:" + readRawValue() + "   ", 0, 4);
            LCD.drawString("tdelta:" + timeDelta + "  ", 0, 5);
            LCD.drawInt(maxTimeDeltaVal, 4, COL2, 5);
            LCD.drawString("hdng:" + readHeading() + "          ", 0, 6);
            Thread.yield();
            doWait(80);
        }
        LCD.drawString("Exiting ...    ", 0, 7);
        doWait(2000);
    }
}
