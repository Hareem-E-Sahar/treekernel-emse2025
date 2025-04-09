package ch.usi.inf.pf2.circuit;

import java.awt.Font;
import java.awt.Graphics;
import ch.usi.inf.pf2.time.Schedule;

public final class NotGate extends Gate {

    private static final long serialVersionUID = 1139667773808282901L;

    private int x1;

    private int y1;

    private int x2;

    private int y2;

    private int x3;

    private int y3;

    private int xInputPin;

    private int yInputPin;

    private int xOutputPin;

    private int yOutputPin;

    public NotGate(final int x, final int y) {
        super();
        this.x1 = x;
        this.y1 = y;
        this.x2 = x1;
        this.y2 = y1 + 30;
        this.x3 = x1 + 50;
        this.y3 = (y1 + y2) / 2;
        this.xInputPin = x1;
        this.yInputPin = y1 + (y2 - y1) / 2;
        this.xOutputPin = x3 + 10;
        this.yOutputPin = y3;
    }

    public void setValue(final Value value, final InputPinForGate inputPin) {
        if (inputPin.equals(this.inputPin1)) {
            valuesInputPin1.put(Schedule.getStep(), value);
        }
    }

    public void compute() {
        if (valuesInputPin1.containsKey(Schedule.getStep() - delay)) {
            values[0] = valuesInputPin1.get(Schedule.getStep() - delay);
        }
        this.valueAtExit = Value.UNKNOWN;
        if (this.values[0] == Value.TRUE) {
            this.valueAtExit = Value.FALSE;
        } else if (this.values[0] == Value.FALSE) {
            this.valueAtExit = Value.TRUE;
        }
        valuesInputPin1.remove(Schedule.getStep() - delay);
        createEvent();
    }

    @Override
    public void setWireInputPin1(final Wire wire) {
        this.wireInputPin1 = wire;
        this.wireInputPin2 = wire;
        inputPin1 = new InputPinForGate(this);
        inputPin2 = new InputPinForGate(this);
        wire.setInputPin(inputPin1);
    }

    @Override
    public void setWireInputPin2(final Wire wire) {
        setWireInputPin1(wire);
    }

    @Override
    public void removeInputPin(InputPinForGate inputPinForGate) {
        if (inputPinForGate.equals(inputPin1)) {
            inputPin1 = null;
            inputPin2 = null;
        }
    }

    public int getOriginX() {
        return (x1 + x3) / 2;
    }

    public int getOriginY() {
        return (y1 + y2) / 2;
    }

    public int getXInputPin1() {
        return this.xInputPin - 5;
    }

    public int getYInputPin1() {
        return this.yInputPin;
    }

    public int getXInputPin2() {
        return this.xInputPin - 5;
    }

    public int getYInputPin2() {
        return this.yInputPin;
    }

    public int getXOutputPin() {
        return this.xOutputPin + 5;
    }

    public int getYOutputPin() {
        return this.yOutputPin;
    }

    public void draw(final Graphics g) {
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x1, y1, x3, y3);
        g.drawLine(x2, y2, x3, y3);
        g.drawOval(x3, (y3 - 5), 10, 10);
        g.drawLine(xInputPin, yInputPin, (xInputPin - 5), yInputPin);
        g.drawLine(xOutputPin, yOutputPin, (xOutputPin + 5), yOutputPin);
        String d = delay + "";
        g.setFont(new Font("TimesRoman", Font.BOLD, 10));
        g.drawString(d, getOriginX() - g.getFontMetrics().stringWidth(d) / 2, y1);
    }

    public boolean contains(final int x, final int y) {
        return x >= x1 && x <= x3 && y >= y1 && y <= y2;
    }

    @Override
    public void move(final int deltaX, final int deltaY) {
        x1 += deltaX;
        x2 += deltaX;
        x3 += deltaX;
        xInputPin += deltaX;
        xOutputPin += deltaX;
        y1 += deltaY;
        y2 += deltaY;
        y3 += deltaY;
        yInputPin += deltaY;
        yOutputPin += deltaY;
        this.moveWire(deltaX, deltaY);
    }

    @Override
    public void moveWire(final int deltaX, final int deltaY) {
        if (wireInputPin1 != null) {
            wireInputPin1.moveXY2(deltaX, deltaY);
        }
        if (wiresOutputPin != null) {
            for (Wire wire : wiresOutputPin) {
                wire.moveXY1(deltaX, deltaY);
            }
        }
    }

    public String toString() {
        return "NOT gate";
    }
}
