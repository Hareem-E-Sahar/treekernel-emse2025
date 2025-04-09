package Lesson5;

import lejos.nxt.*;

/**
 * A sensor that is able to distinguish a black/dark surface
 * from a white/bright surface.
 * 
 * Light percent values from an active light sensor and a
 * threshold value calculated based on a reading over a 
 * black/dark surface and a reading over a light/bright 
 * surface is used to make the distinction between the two 
 * types of surfaces.
 *  
 * @author  Ole Caprani
 * @version 23.08.07
 */
public class ColorBWSensor {

    private LightSensor ls;

    private int blackLightValue;

    private int whiteLightValue;

    private int colorLightValue;

    private int error;

    private int blackWhiteThreshold;

    public ColorBWSensor(SensorPort p) {
        ls = new LightSensor(p);
        ls.setFloodlight(true);
        this.error = 4;
    }

    private int read(String color) {
        int lightValue = 0;
        while (Button.ENTER.isPressed()) ;
        LCD.clear();
        LCD.drawString("Press ENTER", 0, 0);
        LCD.drawString("to callibrate", 0, 1);
        LCD.drawString(color, 0, 2);
        while (!Button.ENTER.isPressed()) {
            lightValue = ls.readValue();
            LCD.drawInt(lightValue, 4, 10, 2);
            LCD.refresh();
        }
        return lightValue;
    }

    public void calibrate() {
        colorLightValue = read("finish color");
        blackLightValue = read("black");
        whiteLightValue = read("white");
        blackWhiteThreshold = (blackLightValue + whiteLightValue) / 2;
    }

    public boolean finishColor() {
        return (ls.readValue() < (colorLightValue + error) && (colorLightValue - error) < ls.readValue());
    }

    public boolean black() {
        return (ls.readValue() < blackWhiteThreshold);
    }

    public boolean white() {
        return (ls.readValue() > blackWhiteThreshold);
    }

    public int light() {
        return ls.readValue();
    }

    public int getOffset() {
        return blackWhiteThreshold;
    }

    public void setColorLightValue(int colorLightValue) {
        this.colorLightValue = colorLightValue;
    }

    public int getColorLightValue() {
        return colorLightValue;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getError() {
        return error;
    }
}
