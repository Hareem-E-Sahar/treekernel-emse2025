package Lesson6;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.RCXLightSensor;

public class Calibration {

    private RCXLightSensor lsL;

    private RCXLightSensor lsM;

    private RCXLightSensor lsR;

    private Motor LEFT;

    private Motor RIGHT;

    private int lRMin, lRMax, lMMin, lMMax, lLMin, lLMax, lLCross, lRCross;

    private int iSLMC;

    protected static int POWER_SCAN = 60;

    int ldRange;

    int ldMid;

    @SuppressWarnings("unused")
    private int lLCrossThresh;

    @SuppressWarnings("unused")
    private int lRCrossThresh;

    private int lLLowThresh;

    @SuppressWarnings("unused")
    private int lRLowThresh;

    private int lMLowThresh;

    private int lLHighThresh;

    @SuppressWarnings("unused")
    private int lRHighThresh;

    private int lMHighThresh;

    private int lALowThresh;

    private int lAHighThresh;

    public Calibration(MotorPort ml, MotorPort mr, SensorPort pl, SensorPort pm, SensorPort pr) {
        lsL = new RCXLightSensor(pl);
        lsM = new RCXLightSensor(pm);
        lsR = new RCXLightSensor(pr);
        lsL.setFloodlight(true);
        lsM.setFloodlight(true);
        lsR.setFloodlight(true);
        this.LEFT = new Motor(ml);
        this.RIGHT = new Motor(mr);
        lLMin = 1023;
        lLMax = 0;
        lRMin = 1023;
        lRMax = 0;
        lMMin = 1023;
        lMMax = 0;
        iSLMC = 0;
    }

    void scanLineMotorControl(int iDir) throws InterruptedException {
        if (iSLMC == 0) {
            if (iDir == -1) {
                LEFT.setPower(POWER_SCAN);
                LEFT.backward();
                RIGHT.setPower(POWER_SCAN);
                RIGHT.forward();
            } else {
                LEFT.setPower(POWER_SCAN);
                LEFT.forward();
                RIGHT.setPower(POWER_SCAN);
                RIGHT.backward();
            }
            iSLMC = 2;
        } else {
            offAll();
            iSLMC--;
        }
        Thread.sleep(10);
    }

    public void scanLineForCal() throws InterruptedException {
        int lL = 0, lM = 0, lR = 0;
        LCD.drawString("Calibration...", 0, 0);
        for (int i = 0; i <= 11; i++) {
            int lLMin_aux = 2046;
            int lLMax_aux = 0;
            int lRMin_aux = 2046;
            int lRMax_aux = 0;
            int lMMin_aux = 2046;
            int lMMax_aux = 0;
            while ((lLMax_aux - lL) < 20) {
                lL = lsL.getNormalizedLightValue();
                if (lL > lLMax_aux) lLMax_aux = lL;
                scanLineMotorControl(1);
            }
            offAll();
            while ((lL - lLMin_aux) < 20) {
                lL = lsL.getNormalizedLightValue();
                if (lL < lLMin_aux) lLMin_aux = lL;
                scanLineMotorControl(1);
            }
            offAll();
            long t1 = System.currentTimeMillis();
            while (Math.abs(System.currentTimeMillis() - t1) < 200) {
                scanLineMotorControl(1);
            }
            offAll();
            while ((lRMax_aux - lR) < 20) {
                lR = lsR.getNormalizedLightValue();
                if (lR > lRMax_aux) lRMax_aux = lR;
                scanLineMotorControl(-1);
            }
            offAll();
            while ((lR - lRMin_aux) < 20) {
                lR = lsR.getNormalizedLightValue();
                if (lR < lRMin_aux) lRMin_aux = lR;
                scanLineMotorControl(-1);
            }
            offAll();
            while ((lMMax_aux - lM) < 20) {
                lM = lsM.getNormalizedLightValue();
                if (lM > lMMax_aux) lMMax_aux = lM;
                scanLineMotorControl(1);
            }
            offAll();
            while ((lM - lMMin_aux) < 20) {
                lM = lsM.getNormalizedLightValue();
                if (lM < lMMin_aux) lMMin_aux = lM;
                scanLineMotorControl(1);
            }
            offAll();
            if (lLMin_aux < lLMin) lLMin = lLMin_aux;
            if (lRMin_aux < lRMin) lRMin = lRMin_aux;
            if (lMMin_aux < lMMin) lMMin = lMMin_aux;
            if (lLMax_aux > lLMax) lLMax = lLMax_aux;
            if (lRMax_aux > lRMax) lRMax = lRMax_aux;
            if (lMMax_aux > lMMax) lMMax = lMMax_aux;
        }
    }

    public void calcLightParameters() {
        int ldHigh;
        int ldLow;
        ldHigh = lLMax - lRMin;
        ldLow = lLMin - lRMax;
        ldRange = (ldHigh - ldLow) / 2;
        ldMid = (ldHigh + ldLow) / 2;
        lLCrossThresh = (lLMin * 2 + lLCross) / 3;
        lRCrossThresh = (lRMin * 2 + lRCross) / 3;
        int mult = 4;
        lLLowThresh = (lLMin * mult + lLMax) / (mult + 1);
        lRLowThresh = (lRMin * mult + lRMax) / (mult + 1);
        lMLowThresh = (lMMin * mult + lMMax) / (mult + 1);
        lLHighThresh = (lLMax * mult + lLMin) / (mult + 1);
        lRHighThresh = (lRMax * mult + lRMin) / (mult + 1);
        lMHighThresh = (lMMax * mult + lMMin) / (mult + 1);
        lALowThresh = ((lLMin + lRMin + lMMin) * mult + (lLMax + lRMax + lMMax)) / (mult + 1);
        lAHighThresh = ((lLMax + lRMax + lMMax) * mult + (lLMin + lRMin + lMMin)) / (mult + 1);
    }

    private void offAll() {
        LEFT.stop();
        RIGHT.stop();
    }

    public int[] getAThresh() {
        int[] at = new int[2];
        at[0] = lALowThresh;
        at[1] = lAHighThresh;
        return at;
    }

    public int[] getLeftThresh() {
        int[] lt = new int[2];
        lt[0] = lLLowThresh;
        lt[1] = lLHighThresh;
        return lt;
    }

    public int[] getRightThresh() {
        int[] rt = new int[2];
        rt[0] = lLLowThresh;
        rt[1] = lLHighThresh;
        return rt;
    }

    public int[] getMiddleThresh() {
        int[] mt = new int[2];
        mt[0] = lMLowThresh;
        mt[1] = lMHighThresh;
        return mt;
    }

    public int[] getLeftValues() {
        int[] lv = new int[2];
        lv[0] = lLMin;
        lv[1] = lLMax;
        return lv;
    }

    public int[] getMiddleValues() {
        int[] mv = new int[2];
        mv[0] = lMMin;
        mv[1] = lMMax;
        return mv;
    }

    public int[] getRightValues() {
        int[] rv = new int[2];
        rv[0] = lRMin;
        rv[1] = lRMax;
        return rv;
    }

    public int getLdMid() {
        return ldMid;
    }

    public int getLdRange() {
        return ldRange;
    }
}
